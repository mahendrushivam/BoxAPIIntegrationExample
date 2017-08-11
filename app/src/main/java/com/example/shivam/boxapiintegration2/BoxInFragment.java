package com.example.shivam.boxapiintegration2;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.BoxConstants;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxEntity;
import com.box.androidsdk.content.models.BoxError;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxListItems;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.requests.BoxRequestsFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class BoxInFragment extends Fragment implements BoxAuthentication.AuthListener{
    BoxSession mSession = null;
    BoxSession mOldSession = null;


    private ExpandableListView expandableListView;
    private ProgressDialog mDialog;

    //private ArrayAdapter<BoxItem> mAdapter;
ArrayList<BoxItem> listArrayAdapter=null;
    private BoxApiFolder mFolderApi;
    private BoxApiFile mFileApi;
    BoxListAdapter boxListAdapter=null;
    public BoxInFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_box_in, container, false);
        expandableListView = (ExpandableListView) v.findViewById(R.id.boxlistview);
        listArrayAdapter = new ArrayList<BoxItem>();
        boxListAdapter=new BoxListAdapter(getContext(),listArrayAdapter,mSession);
        BoxConfig.IS_LOG_ENABLED = true;
        configureClient();
        initSession();
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Toast.makeText(getContext(),"Open",Toast.LENGTH_LONG).show();
                GridView gridView=(GridView)v.findViewById(R.id.childfileview);

                return true;
            }
        });
        return v;
    }
    private void configureClient() {
        BoxConfig.CLIENT_ID = "b5nndupon7mphjgavmwur0ih5k5ee1i1";
        BoxConfig.CLIENT_SECRET = "LgjwRbZCFSIPfVRxfS0NXFb5Jh58J9IL";

        // needs to match redirect uri in developer settings if set.
        //   BoxConfig.REDIRECT_URL = "<YOUR_REDIRECT_URI>";
    }

    /**
     * Create a BoxSession and authenticate.
     */
    private void initSession() {
        listArrayAdapter.clear();
        boxListAdapter.updateArrayAdapter(listArrayAdapter);
        boxListAdapter.notifyDataSetChanged();

        expandableListView.setAdapter(boxListAdapter);
        mSession = new BoxSession(getContext());
        boxListAdapter.updateSession(mSession);
        mSession.setSessionAuthListener(this);
        mSession.authenticate();
    }

    @Override
    public void onRefreshed(BoxAuthentication.BoxAuthenticationInfo info) {
        // do nothing when auth info is refreshed
    }

    @Override
    public void onAuthCreated(BoxAuthentication.BoxAuthenticationInfo info) {
        //Init file, and folder apis; and use them to fetch the root folder
        mFolderApi = new BoxApiFolder(mSession);
        mFileApi = new BoxApiFile(mSession);
        loadRootFolder();
    }

    @Override
    public void onAuthFailure(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
        if (ex != null) {
            clearAdapter();
        } else if (info == null && mOldSession != null) {
            mSession = mOldSession;
            mSession.setSessionAuthListener(this);
            boxListAdapter.updateSession(mSession);
            mOldSession = null;
            onAuthCreated(mSession.getAuthInfo());
        }
    }

    @Override
    public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
        clearAdapter();
        initSession();
    }


    //Method to demonstrate fetching folder items from the root folder
    private void loadRootFolder() {
        new Thread() {
            @Override
            public void run() {
                try {
                    //Api to fetch root folder
                    final BoxListItems folderItems = mFolderApi.getItemsRequest(BoxConstants.ROOT_FOLDER_ID).send();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (BoxItem boxItem: folderItems) {
                                listArrayAdapter.add(boxItem);
                                //mAdapter.add(boxItem);
                            }
                            boxListAdapter.updateArrayAdapter(listArrayAdapter);
                            expandableListView.setAdapter(boxListAdapter);

                        }
                    });
                } catch (BoxException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    /**
     * Method demonstrates a sample file being uploaded using the file api
     */
    private void uploadSampleFile() {
        mDialog = ProgressDialog.show(getContext(), getText(R.string.boxsdk_Please_wait), getText(R.string.boxsdk_Please_wait));
        new Thread() {
            @Override
            public void run() {
                try {
                    String uploadFileName = "box_logo.png";
                    InputStream uploadStream = getResources().getAssets().open(uploadFileName);
                    String destinationFolderId = "0";
                    String uploadName = "BoxSDKUpload.png";
                    BoxRequestsFile.UploadFile request = mFileApi.getUploadRequest(uploadStream, uploadName, destinationFolderId);
                    final BoxFile uploadFileInfo = request.send();
                    showToast("Uploaded " + uploadFileInfo.getName());
                    loadRootFolder();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BoxException e) {
                    e.printStackTrace();
                    BoxError error = e.getAsBoxError();
                    if (error != null) {
                        ArrayList<BoxEntity> conflicts = error.getContextInfo().getConflicts();
                        if (conflicts != null && conflicts.size() == 1 && conflicts.get(0) instanceof BoxFile) {
                            uploadNewVersion((BoxFile) conflicts.get(0));
                            return;
                        }
                    }
                    showToast("Upload failed");
                } finally {
                    mDialog.dismiss();
                }
            }
        }.start();

    }

    /**
     * Method demonstrates a new version of a file being uploaded using the file api
     * @param file
     */
    private void uploadNewVersion(final BoxFile file) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String uploadFileName = "box_logo.png";
                    InputStream uploadStream = getResources().getAssets().open(uploadFileName);
                    BoxRequestsFile.UploadNewVersion request = mFileApi.getUploadNewVersionRequest(uploadStream, file.getId());
                    final BoxFile uploadFileVersionInfo = request.send();
                    showToast("Uploaded new version of " + uploadFileVersionInfo.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BoxException e) {
                    e.printStackTrace();
                    showToast("Upload failed");
                } finally {
                    mDialog.dismiss();
                }
            }
        }.start();
    }

    private void showToast(final String text) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
            }
        });
    }


    private void clearAdapter() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                listArrayAdapter.clear();
                boxListAdapter.updateArrayAdapter(listArrayAdapter);
                expandableListView.setAdapter(boxListAdapter);

            }
        });
    }



}
