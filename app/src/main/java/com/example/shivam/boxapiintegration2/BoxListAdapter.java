package com.example.shivam.boxapiintegration2;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.preview.BoxPreviewActivity;

import java.util.ArrayList;

/**
 * Created by Shivam on 4/14/2017.
 */

public class BoxListAdapter  extends BaseExpandableListAdapter{

ArrayList<BoxItem> boxItems=null;
    public static final int PREVIEW_CODE=01;
    Context mcontext;
    GridView childLayout2;
    BoxSession sesssion;
LayoutInflater mLayoutInflater;
    String [] arrFolderOptions={"Open","Download","Delete","Compress"};
    //String [] arrFileOptions={"Open","Download","Delete","Compress"};
    int [] imageresource={R.drawable.open,R.drawable.clouddownload,R.drawable.deleteicon,R.drawable.compressicon};

    public BoxListAdapter(Context context, ArrayList<BoxItem> boxItems, BoxSession session)
    {
        mcontext=context;
        boxItems=new ArrayList<BoxItem>();
        this.boxItems=boxItems;
        this.sesssion=session;
        mLayoutInflater = (LayoutInflater) mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getGroupCount() {
        return boxItems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        BoxItem boxItem=boxItems.get(groupPosition);
        if(boxItem instanceof BoxFolder)
            return 1;
        else
            return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return boxItems.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

    return childPosition;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            convertView = mLayoutInflater.inflate(R.layout.boxsdk_list_item, parent, false);
        }
        BoxItem item=boxItems.get(groupPosition);
        TextView name = (TextView) convertView.findViewById(R.id.name);
        name.setText(item.getName());

        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        if (item instanceof BoxFolder) {
            icon.setImageResource(R.mipmap.ic_launcher);
        } else {
            icon.setImageResource(R.mipmap.ic_launcher);
        }
        final ImageView expandedImage = (ImageView) convertView.findViewById(R.id.swipebtn);
        final int resId = isExpanded ? R.drawable.uparrow :R.drawable.downarrow ;
        expandedImage.setImageResource(resId);


        return convertView;
    }
    public void updateSession(BoxSession session)
    {
        this.sesssion=session;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if(convertView==null)
        {
            convertView = mLayoutInflater.inflate(R.layout.childfileviewlayout, parent, false);
        }
        childLayout2=(GridView)convertView.findViewById(R.id.childfileview);
        MyArrayAdapter arrayAdapter=new MyArrayAdapter(mcontext,R.layout.iconlayout,R.id.textview,arrFolderOptions);
        childLayout2.setAdapter(arrayAdapter);
        childLayout2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0)

                {BoxFile file=null;
                    //BoxApiFile fileApi = new BoxApiFile(sesssion);
                    BoxItem boxItem=boxItems.get(groupPosition);
                    String id1=boxItem.getId();
                    BoxApiFile fileApi = new BoxApiFile(sesssion);
                    try
                    {
                        file = fileApi.getInfoRequest(id1).send();
                    }
                    catch (BoxException e)
                    {
                        e.printStackTrace();
                    }
                    BoxFolder parentFolder = file.getParent() == null ? BoxFolder.createFromId("0") : file.getParent();
                    BoxPreviewActivity.IntentBuilder builder = BoxPreviewActivity.createIntentBuilder(mcontext, sesssion, file).setBoxFolder(parentFolder);
                    if (mcontext instanceof Activity) {
                        ((Activity) mcontext).startActivityForResult(builder.createIntent(),PREVIEW_CODE);


                }}

                else if(position==1)

                {


                }

                else if(position==2)
                {
                    BoxItem boxItem=boxItems.get(groupPosition);
                    BoxApiFile fileApi = new BoxApiFile(sesssion);
                    String id1=boxItem.getId();
                    try {
                        fileApi.getDeleteRequest(id1).send();
                    }
                    catch (BoxException e) {
                        e.printStackTrace();
                    }



                }

                else if(position==3)
                {


                }
            }
        });
        return convertView;
    }



    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    public void updateArrayAdapter(ArrayList<BoxItem> boxItems)
    {
        this.boxItems=boxItems;
    }

    public class MyArrayAdapter extends ArrayAdapter<String>
    {

        public MyArrayAdapter(Context context,int layout, int resourceid,String [] str) {
            super(context,layout, resourceid,str);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View row = super.getView(position, convertView, parent);
            ViewHolder holder = (ViewHolder) row.getTag();
            if (holder == null) {
                holder = new ViewHolder(row);
                row.setTag(holder);
            }

            holder.imageView.setImageResource(imageresource[position]);
            holder.textView.setText("");


           return row;
        }
    }

    public class ViewHolder
    {
        ImageView imageView;
        TextView textView;
        public ViewHolder(View row)
        {
            imageView=(ImageView)row.findViewById(R.id.imageicon);
            textView=(TextView)row.findViewById(R.id.textview);
        }

    }


}
