package com.example.prm_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm_project.R;
import com.example.prm_project.model.Book;

import java.util.List;

public class RankingBookAdapter extends RecyclerView.Adapter<RankingBookAdapter.RankingViewHolder> {

    private List<Book> bookList;
    private BookAdapter.OnBookClickListener listener;

    public RankingBookAdapter(List<Book> bookList, BookAdapter.OnBookClickListener listener) {
        this.bookList = bookList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking_book, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.bind(book, position + 1);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    class RankingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRankingNumber, tvBookTitle, tvBookAuthor, tvBookPrice;
        private ImageView ivBookCover;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRankingNumber = itemView.findViewById(R.id.tvRankingNumber);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvBookPrice = itemView.findViewById(R.id.tvBookPrice);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
        }

        public void bind(Book book, int ranking) {
            tvRankingNumber.setText(String.valueOf(ranking));
            tvBookTitle.setText(book.getName());
            tvBookAuthor.setText(book.getAuthor());
            tvBookPrice.setText(String.format("$%,d", book.getPrice()));

            // Load book cover
            if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(book.getImageUrl())
                        .placeholder(R.drawable.default_book_cover)
                        .error(R.drawable.default_book_cover)
                        .into(ivBookCover);
            } else {
                ivBookCover.setImageResource(R.drawable.default_book_cover);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(book);
                }
            });
        }
    }
}