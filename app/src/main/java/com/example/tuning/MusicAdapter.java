package com.example.tuning;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyVieHolder> {

    private Context mcontext;
    private ArrayList<MusicFiles> mfiles;

    public MusicAdapter(Context mcontext, ArrayList<MusicFiles> mfiles) {
        this.mcontext = mcontext;
        this.mfiles = mfiles;
    }

    @NonNull
    @Override
    public MyVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.music_items,parent,false);
        return new MyVieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVieHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.file_name.setText(mfiles.get(position).getTitle());
        byte[] image = getAlbumArt(mfiles.get(position).getPath());
        if(image!=null){
            Glide.with(mcontext).asBitmap()
                    .load(image).into(holder.album_art);
        }
        else{
            Glide.with(mcontext).load(R.drawable.bewedoc)
                    .into(holder.album_art);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mcontext,PlayerActivity.class);
                intent.putExtra("position",position);
                mcontext.startActivity(intent);
            }
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mcontext,v);
                popupMenu.getMenuInflater().inflate(R.menu.popup,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener((item) ->
                {
                    switch (item.getItemId()){
                        case R.id.delete:
                            Toast.makeText(mcontext,"Delete Clicked!",Toast.LENGTH_SHORT).show();
                            deleteFile(position,v);
                            break;
                    }
                    return true;
                });
            }
        });
    }

    private void deleteFile(int position, View v)
    {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mfiles.get(position).getId()));//content
        File file = new File((mfiles.get(position).getPath()));
        boolean deleted = file.delete();//delete your file
        if(deleted) {
            mcontext.getContentResolver().delete(contentUri,null,null);
            mfiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mfiles.size());
            Snackbar.make(v, "File Deleted:", Snackbar.LENGTH_SHORT).show();
        }
        else{

            //may be file in sd card
            Snackbar.make(v, "File can't be Deleted:", Snackbar.LENGTH_SHORT).show();
        }
    }


    @Override
    public int getItemCount() {
        return mfiles.size();
    }

    public  class MyVieHolder extends RecyclerView.ViewHolder{
        TextView file_name;
        ImageView album_art,menuMore;
        public MyVieHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            album_art = itemView.findViewById(R.id.music_img);
            menuMore  = itemView.findViewById(R.id.menuMore);
        }
    }
    private  byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever= new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        try {
            retriever.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return art;
    }
}
