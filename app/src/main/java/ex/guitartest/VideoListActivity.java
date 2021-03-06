package ex.guitartest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ex.guitartest.video.Code;
import ex.guitartest.video.CommTools;
import ex.guitartest.video.VideoInfo;


public class VideoListActivity extends AppCompatActivity {

    private ListView mListView;
    private TextView noData;
    private Button btnBack;
    ArrayList<VideoInfo> vList;
    private Intent lastIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        mListView =  findViewById(R.id.lv_video);
        noData =  findViewById(R.id.tv_nodata);
        btnBack = findViewById(R.id.btn_back);

        lastIntent = getIntent();
        initData();
        mListView.setOnItemClickListener(new ItemClick());
    }

    private void initData() {
        vList = new ArrayList<VideoInfo>();
        String[] mediaColumns = new String[]{MediaStore.MediaColumns.DATA,
                BaseColumns._ID, MediaStore.MediaColumns.TITLE, MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.Video.VideoColumns.DURATION, MediaStore.MediaColumns.SIZE};
        Cursor cursor = getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaColumns,
                null, null, null);
        if (cursor.moveToFirst()) {
            do {
                VideoInfo info = new VideoInfo();
                info.setFilePath(cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)));
                info.setMimeType(cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)));
                info.setTitle(cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE)));
                info.setTime(CommTools.LongToHms(cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION))));
                info.setSize(CommTools.LongToPoint(cursor
                        .getLong(cursor
                                .getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE))));
                int id = cursor.getInt(cursor
                        .getColumnIndexOrThrow(BaseColumns._ID));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                info.setB(MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), id,
                        MediaStore.Images.Thumbnails.MICRO_KIND, options));
                vList.add(info);

            } while (cursor.moveToNext());
        }

        if (vList.size() == 0) {
            mListView.setVisibility(View.GONE);
            noData.setVisibility(View.VISIBLE);
        } else {
            for(int i = vList.size()-1; i >=0 ; i--)
            {
                if (!vList.get(i).getFilePath().contains(getString(R.string.folder)))
                    vList.remove(i);
            }
            VideoListAdapter adapter = new VideoListAdapter(VideoListActivity.this);
            mListView.setAdapter(adapter);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        cursor.close();
    }

    private class ItemClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mListView.getItemAtPosition(position);
            String filePath = vList.get(position).getFilePath();
            lastIntent.putExtra("path", filePath);
            setResult(Code.LOCAL_VIDEO_RESULT, lastIntent);
            finish();
        }

    }

    class VideoListAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public VideoListAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return vList.size();
        }

        @Override
        public Object getItem(int p) {
            return vList.get(p);
        }

        @Override
        public long getItemId(int p) {
            return p;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_video_list, null);
                holder.vImage =  convertView.findViewById(R.id.video_img);
                holder.vTitle = convertView.findViewById(R.id.video_title);
                holder.vSize = convertView.findViewById(R.id.video_size);
                holder.vTime = convertView.findViewById(R.id.video_time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.vImage.setImageBitmap(vList.get(position).getB());
            holder.vTitle.setText(vList.get(position).getTitle()); // + "." + (videoList.get(position).getMimeType()).substring(6))
            holder.vSize.setText(vList.get(position).getSize());
            holder.vTime.setText(vList.get(position).getTime());

            holder.vImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String bpath = "file://" + vList.get(position).getFilePath();
                    intent.setDataAndType(Uri.parse(bpath), "video/*");
                    startActivity(intent);
                }
            });
            return convertView;
        }

        class ViewHolder {
            ImageView vImage;
            TextView vTitle;
            TextView vSize;
            TextView vTime;
        }
    }

}
