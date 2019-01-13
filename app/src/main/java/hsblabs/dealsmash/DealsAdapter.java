package hsblabs.dealsmash;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad Haseeb on 2/18/2017.
 */

public class DealsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<Object> dealslist;
    //    private List<NativeExpressAdView> adsList = new ArrayList<>();
    int count = 0;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, des;
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            des = (TextView) view.findViewById(R.id.des);
            thumbnail = (ImageView) view.findViewById(R.id.img);
        }
    }

    public static class ViewHolderAdMob extends RecyclerView.ViewHolder {
        public ViewHolderAdMob(View view) {
            super(view);
        }
    }

    public DealsAdapter(Context mContext, List<Object> dealslist) {
        this.mContext = mContext;
        this.dealslist = dealslist;
//        this.adsList = adsList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item, parent, false);
        viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {


        switch (holder.getItemViewType()) {
            case 0: {
                final DealItem album = (DealItem) dealslist.get(holder.getAdapterPosition());
                final MyViewHolder viewHolder = (MyViewHolder) holder;
                viewHolder.title.setText(album.getTitle());
                Glide.with(mContext)
                        .load(album.getImage())
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                viewHolder.thumbnail.setImageBitmap(resource);
                            }
                        });
                viewHolder.des.setText(album.getDesc());
                viewHolder.thumbnail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });

                break;
            }
        }

    }

    @Override
    public int getItemCount() {
        return dealslist.size();
    }

}