package com.example.prm_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm_project.R;
import com.example.prm_project.model.BannerItem;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<BannerItem> bannerList;
    private OnBannerClickListener listener;

    public interface OnBannerClickListener {
        void onBannerClick(BannerItem banner);
    }

    public BannerAdapter(List<BannerItem> bannerList, OnBannerClickListener listener) {
        this.bannerList = bannerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        // Đảm bảo view có kích thước match_parent
        view.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        BannerItem banner = bannerList.get(position);
        holder.bind(banner);
    }

    @Override
    public int getItemCount() {
        return bannerList != null ? bannerList.size() : 0;
    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBanner;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.ivBanner);
        }

        public void bind(BannerItem banner) {
            if (banner != null) {
                Glide.with(itemView.getContext())
                    .load(banner.getImageUrl())
                    .placeholder(R.drawable.banner_placeholder)
                    .error(R.drawable.banner_placeholder)
                    .centerCrop()
                    .into(ivBanner);

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onBannerClick(banner);
                    }
                });
            }
        }
    }
}