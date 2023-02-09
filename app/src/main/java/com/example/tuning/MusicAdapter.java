package com.example.tuning;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
    public void onBindViewHolder(@NonNull MyVieHolder holder, int position) {
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
                mcontext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mfiles.size();
    }

    public  class MyVieHolder extends RecyclerView.ViewHolder{
        TextView file_name;
        ImageView album_art;
        public MyVieHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            album_art= itemView.findViewById(R.id.music_img);
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
