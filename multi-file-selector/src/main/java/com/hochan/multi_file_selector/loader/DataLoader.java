package com.hochan.multi_file_selector.loader;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.hochan.multi_file_selector.data.Folder;
import com.hochan.multi_file_selector.data.MediaFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/7.
 */
public class DataLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,          //0
            MediaStore.Images.Media.DISPLAY_NAME,  //1
            MediaStore.Images.Media.DATE_ADDED,    //2
            MediaStore.Images.Media.SIZE,          //3
            MediaStore.Images.Media.MIME_TYPE,     //4
            MediaStore.Images.Media._ID };         //5
//
//    private final String[] VIDEO_PROJECTION = {
//            MediaStore.Video.Media.DATA,           //0
//            MediaStore.Video.Media.DISPLAY_NAME,   //1
//            MediaStore.Video.Media.DATE_ADDED,     //2
//            MediaStore.Video.Media.MIME_TYPE,      //3
//            MediaStore.Video.Media.SIZE,           //4
//            MediaStore.Video.Media._ID,            //5
//            MediaStore.Video.Media.DURATION};      //6
//
//    private final String[] AUDIO_PROJECTION = {
//            MediaStore.Audio.Media.DATA,           //0
//            MediaStore.Audio.Media.DISPLAY_NAME,   //1
//            MediaStore.Audio.Media.DATE_ADDED,     //2
//            MediaStore.Audio.Media.MIME_TYPE,      //3
//            MediaStore.Audio.Media.SIZE,           //4
//            MediaStore.Audio.Media._ID,            //5
//            MediaStore.Audio.Media.ARTIST,         //6
//            MediaStore.Audio.Media.DURATION};      //7

    private static ArrayList<String> IMAGE_PROJECTION_LIST = new ArrayList<>();
    private static ArrayList<String> AUDIO_PROJECTION_LIST = new ArrayList<>();
    private static ArrayList<String> VIDEO_PROJECTION_LIST = new ArrayList<>();
    private static ArrayList<ArrayList<String>> MEDIA_PROJECTION_LIST = new ArrayList<>();

    static {
        IMAGE_PROJECTION_LIST.add(MediaStore.Images.Media.DATA);
        IMAGE_PROJECTION_LIST.add(MediaStore.Images.Media.DISPLAY_NAME);
        IMAGE_PROJECTION_LIST.add(MediaStore.Images.Media.DATE_ADDED);
        IMAGE_PROJECTION_LIST.add(MediaStore.Images.Media.SIZE);
        IMAGE_PROJECTION_LIST.add(MediaStore.Images.Media.MIME_TYPE);
        IMAGE_PROJECTION_LIST.add(MediaStore.Images.Media._ID);

        AUDIO_PROJECTION_LIST.add(MediaStore.Audio.Media.DATA);
        AUDIO_PROJECTION_LIST.add(MediaStore.Audio.Media.DISPLAY_NAME);
        AUDIO_PROJECTION_LIST.add(MediaStore.Audio.Media.DATE_ADDED);
        AUDIO_PROJECTION_LIST.add(MediaStore.Audio.Media.SIZE);
        AUDIO_PROJECTION_LIST.add(MediaStore.Audio.Media.MIME_TYPE);
        AUDIO_PROJECTION_LIST.add(MediaStore.Audio.Media._ID);
        AUDIO_PROJECTION_LIST.add(MediaStore.Audio.Media.ARTIST);
        AUDIO_PROJECTION_LIST.add(MediaStore.Audio.Media.DURATION);

        VIDEO_PROJECTION_LIST.add(MediaStore.Video.Media.DATA);
        VIDEO_PROJECTION_LIST.add(MediaStore.Video.Media.DISPLAY_NAME);
        VIDEO_PROJECTION_LIST.add(MediaStore.Video.Media.DATE_ADDED);
        VIDEO_PROJECTION_LIST.add(MediaStore.Video.Media.SIZE);
        VIDEO_PROJECTION_LIST.add(MediaStore.Video.Media.MIME_TYPE);
        VIDEO_PROJECTION_LIST.add(MediaStore.Video.Media._ID);
        VIDEO_PROJECTION_LIST.add(MediaStore.Video.Media.DURATION);

        MEDIA_PROJECTION_LIST.add(MediaFile.TYPE_IMAGE, IMAGE_PROJECTION_LIST);
        MEDIA_PROJECTION_LIST.add(MediaFile.TYPE_AUDIO, AUDIO_PROJECTION_LIST);
        MEDIA_PROJECTION_LIST.add(MediaFile.TYPE_VIDEO, VIDEO_PROJECTION_LIST);
    }

    private Context mContext;
    private int mType;
    private List<Folder> mFolders;
    private List<MediaFile> mMediaFiles;

    public DataLoader(Context context, int type){
        this.mContext = context;
        this.mType = type;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (mType){
            case MediaFile.TYPE_IMAGE:
                System.out.println(IMAGE_PROJECTION_LIST.get(3));
                System.out.println(IMAGE_PROJECTION_LIST.get(4));
                System.out.println(IMAGE_PROJECTION_LIST.get(2));
                CursorLoader acursorLoader = new CursorLoader(mContext,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        (String[]) IMAGE_PROJECTION_LIST.toArray(new String[IMAGE_PROJECTION_LIST.size()]),
                        /*IMAGE_PROJECTION,*/ null, null, null);
                        //?对应后面的selectionArgs用于转义特殊字符
                        //IMAGE_PROJECTION_LIST.get(3)+">0 AND "+IMAGE_PROJECTION_LIST.get(4)+"=? OR "+IMAGE_PROJECTION_LIST.get(4)+"=? ",
                        //new String[]{"image/jpeg", "image/png"}, IMAGE_PROJECTION_LIST.get(2) + " DESC");
                return acursorLoader;
            case MediaFile.TYPE_VIDEO:
                CursorLoader bcursorLoader = new CursorLoader(mContext,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        (String[]) VIDEO_PROJECTION_LIST.toArray(),
                        VIDEO_PROJECTION_LIST.get(3)+">0 AND ",
                        null, VIDEO_PROJECTION_LIST.get(2)+" DESC");
                return bcursorLoader;
            case MediaFile.TYPE_AUDIO:
                CursorLoader ccursorLoader = new CursorLoader(mContext,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        (String[]) AUDIO_PROJECTION_LIST.toArray(),
                        AUDIO_PROJECTION_LIST.get(3)+">0 AND ",
                        null, AUDIO_PROJECTION_LIST.get(2)+" DESC");
                break;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null) {
            if (data.getCount() > 0) {
                System.out.println("corsur不为空:"+data.getCount());
                data.moveToFirst();
                mFolders = new ArrayList<>();
                mMediaFiles = new ArrayList<>(data.getCount());
                do {
                    String displayName = data.getString(
                            data.getColumnIndexOrThrow(MEDIA_PROJECTION_LIST.get(mType).get(1)));
                    String path = data.getString(
                            data.getColumnIndexOrThrow(MEDIA_PROJECTION_LIST.get(mType).get(0)));
                    String dateAdded = data.getString(
                            data.getColumnIndexOrThrow(MEDIA_PROJECTION_LIST.get(mType).get(2)));
                    String size = data.getString(
                            data.getColumnIndexOrThrow(MEDIA_PROJECTION_LIST.get(mType).get(3)));
                    MediaFile mediaFile = null;
                    switch (mType) {
                        case MediaFile.TYPE_IMAGE:
                            mediaFile = new MediaFile(MediaFile.TYPE_IMAGE,
                                    displayName, path, dateAdded, size);
                            break;
                        case MediaFile.TYPE_AUDIO:
                            break;
                        case MediaFile.TYPE_VIDEO:
                            break;
                    }
                    File folderFile = new File(path).getParentFile();
                    if (folderFile.exists()) {
                        if (folderFile != null && folderFile.exists()) {
                            Folder tmpFolder = getFolderByPath(folderFile.getAbsolutePath());
                            if (tmpFolder == null) {
                                List<MediaFile> mediaFiles = new ArrayList<>();
                                mediaFiles.add(mediaFile);
                                Folder folder = new Folder(
                                        folderFile.getName(), folderFile.getAbsolutePath(), mediaFiles);
                                mFolders.add(folder);
                            } else {
                                tmpFolder.getmMediaFiles().add(mediaFile);
                            }
                        }
                    }
                } while (data.moveToNext());
                if (mCallBack != null) {
                    mCallBack.finish(mMediaFiles, mFolders);
                }
            } else {
                System.out.println("查询结果为空" + data.getCount());
            }
        }else {
            System.out.println("cursor为空");
        }
    }

//    private void handleVideoData(Cursor data) {
//        ArrayList<MediaFile> videoFiles = new ArrayList<>();
//        data.moveToFirst();
//        do{
//            String path = data.getString(
//                    data.getColumnIndexOrThrow(VIDEO_PROJECTION[0]));
//            String name = data.getString(
//                    data.getColumnIndexOrThrow(VIDEO_PROJECTION[1]));
//            String dateAdded = data.getString(
//                    data.getColumnIndexOrThrow(VIDEO_PROJECTION[2]));
//            String size = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[4]));
//            MediaFile videoFile = new MediaFile(
//                    MediaFile.TYPE_VIDEO, name, path, dateAdded, size);
//            videoFile.setmDuration(
//                    data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[6])));
//            videoFiles.add(videoFile);
//        }while (data.moveToNext());
//        if(mCallBack != null)
//            mCallBack.finish(videoFiles, mFolders);
//    }

//    private void handleAudioData(Cursor data) {
//        mFolders = new ArrayList<>();
//        ArrayList<MediaFile> audioFiles = new ArrayList<>();
//        data.moveToFirst();
//        do{
//            String path = data.getString(data.getColumnIndexOrThrow(AUDIO_PROJECTION[0]));
//
//        } while (data.moveToNext());
//    }

//    private void handleImageData(Cursor data) {
//        mFolders = new ArrayList<>();
//        ArrayList<MediaFile> imageFiles = new ArrayList<>();
//        data.moveToFirst();
//        do{
//            String path = data.getString(
//                    data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
//            String name = data.getString(
//                    data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
//            String dataAdded = data.getString(
//                    data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
//            String size = data.getString(
//                    data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
//            MediaFile imageFile = new MediaFile(
//                    MediaFile.TYPE_IMAGE, name, path, dataAdded, size);
//            imageFiles.add(imageFile);
//            File folderFile = new File(path).getParentFile();
//            if(folderFile.exists()){
//                if(folderFile != null && folderFile.exists()){
//                    Folder tmpFolder = getFolderByPath(folderFile.getAbsolutePath());
//                    if(tmpFolder == null){
//                        List<MediaFile> mediaFiles = new ArrayList<>();
//                        mediaFiles.add(imageFile);
//                        Folder folder = new Folder(
//                                folderFile.getName(), folderFile.getAbsolutePath(), mediaFiles);
//                        mFolders.add(folder);
//                    }else{
//                        tmpFolder.getmMediaFiles().add(imageFile);
//                    }
//                }
//            }
//        }while (data.moveToNext());
//        if (mCallBack != null)
//            mCallBack.finish(imageFiles, mFolders);
//    }

    private Folder getFolderByPath(String path){
        for(Folder folder : mFolders){
            if(path.equals(folder.getmPath()))
                return folder;
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public interface DataLoaderCallBack{
        public void finish(List<MediaFile> mediaFiles, List<Folder> folders);
    }

    private DataLoaderCallBack mCallBack;

    public void setCallBack(DataLoaderCallBack callBack){
        this.mCallBack = callBack;
    }
}
