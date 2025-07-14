package com.example.prm_project.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.prm_project.R;
import com.example.prm_project.adapter.BannerAdapter;
import com.example.prm_project.adapter.BookAdapter;
import com.example.prm_project.adapter.CategoryAdapter;
import com.example.prm_project.adapter.RankingBookAdapter;
import com.example.prm_project.model.BannerItem;
import com.example.prm_project.model.Book;
import com.example.prm_project.model.BookType;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // UI Components
    private TextView tvUserName, tvGreeting;
    private CircleImageView ivUserAvatar;
    private EditText edSearch;
    private ViewPager2 vpBanner;
    private TabLayout tabIndicator;
    private RecyclerView rvCategories, rvFeaturedBooks, rvRankingBooks;
    private ImageView ivNotification, ivCart;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Adapters
    private BannerAdapter bannerAdapter;
    private CategoryAdapter categoryAdapter;
    private BookAdapter featuredBooksAdapter;
    private RankingBookAdapter rankingBooksAdapter;

    // Data lists
    private List<BannerItem> bannerList = new ArrayList<>();
    private List<BookType> categoryList = new ArrayList<>();
    private List<Book> featuredBooksList = new ArrayList<>();
    private List<Book> rankingBooksList = new ArrayList<>();

    // Auto slide handler
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private int currentPage = 0;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initViews(view);

        // Setup components
        setupUserInfo();
        setupBanner();
        setupRecyclerViews();
        setupSearchFunctionality();
        setupClickListeners();

        // Load data
        loadData();

        return view;
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        ivUserAvatar = view.findViewById(R.id.ivUserAvatar);
        edSearch = view.findViewById(R.id.edSearch);
        vpBanner = view.findViewById(R.id.vpBanner);
        tabIndicator = view.findViewById(R.id.tabIndicator);
        rvCategories = view.findViewById(R.id.rvCategories);
        rvFeaturedBooks = view.findViewById(R.id.rvFeaturedBooks);
        rvRankingBooks = view.findViewById(R.id.rvRankingBooks);
        ivNotification = view.findViewById(R.id.ivNotification);
        ivCart = view.findViewById(R.id.ivCart);
    }

    private void setupUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Set user name
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvUserName.setText(displayName);
            } else {
                String email = currentUser.getEmail();
                if (email != null) {
                    tvUserName.setText(email.split("@")[0]);
                }
            }
            
            // Set greeting
            setGreeting();
            
            // Load user avatar with new drawable
            loadUserAvatar(currentUser);
        } else {
            tvUserName.setText("Khách");
            tvGreeting.setText("Chào mừng!");
            ivUserAvatar.setImageResource(R.drawable.default_avatar_circle);
        }
    }

    private void setGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour >= 5 && hour < 12) {
            greeting = "Chào buổi sáng! ☀️";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Chào buổi chiều! 🌤️";
        } else {
            greeting = "Chào buổi tối! 🌙";
        }

        tvGreeting.setText(greeting);
    }

    private void loadUserAvatar(FirebaseUser user) {
        if (user.getPhotoUrl() != null) {
            // Load from Firebase profile
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.default_avatar_circle)
                    .error(R.drawable.default_avatar_circle)
                    .circleCrop()
                    .into(ivUserAvatar);
        } else {
            // Try to load from Firestore user profile
            db.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String avatarUrl = documentSnapshot.getString("avatarUrl");
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(avatarUrl)
                                        .placeholder(R.drawable.default_avatar_circle)
                                        .error(R.drawable.default_avatar_circle)
                                        .circleCrop()
                                        .into(ivUserAvatar);
                            } else {
                                ivUserAvatar.setImageResource(R.drawable.default_avatar_circle);
                            }
                        } else {
                            ivUserAvatar.setImageResource(R.drawable.default_avatar_circle);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading user avatar", e);
                        ivUserAvatar.setImageResource(R.drawable.default_avatar_circle);
                    });
        }
    }

    private void setupBanner() {
        // Tạo data mẫu với constructor đúng (int, String, String)
        if (bannerList.isEmpty()) {
            bannerList.add(new BannerItem(1, "Banner 1", "https://cdn0.fahasa.com/media/magentothem/banner7/trangct2_152_840x320.jpg"));
            bannerList.add(new BannerItem(2, "Banner 2", "https://cdn0.fahasa.com/media/wysiwyg/Thang-02-2025/MCBooks_Vang_840x320%20_2.png"));
            bannerList.add(new BannerItem(3, "Banner 3", "https://cdn1.fahasa.com/media/magentothem/banner7/TrangCT_0725_Resize_840x320.png"));
        }

        // Setup adapter
        bannerAdapter = new BannerAdapter(bannerList, banner -> {
            // Handle banner click
            Toast.makeText(getContext(), "Clicked: " + banner.getTitle(), Toast.LENGTH_SHORT).show();
        });
        
        vpBanner.setAdapter(bannerAdapter);
        vpBanner.setOffscreenPageLimit(3);

        // Setup tab indicator
        new TabLayoutMediator(tabIndicator, vpBanner, (tab, position) -> {
            // Tab được tạo tự động
        }).attach();

        // Setup auto slide
        setupAutoSlide();
    }

    private void setupAutoSlide() {
        if (vpBanner == null || bannerList.isEmpty()) return;
        
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                if (vpBanner != null && bannerList.size() > 0) {
                    currentPage = (currentPage + 1) % bannerList.size();
                    vpBanner.setCurrentItem(currentPage, true);
                    sliderHandler.postDelayed(this, 3000);
                }
            }
        };
        
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView - Horizontal
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(categoryList, this::onCategoryClick);
        rvCategories.setAdapter(categoryAdapter);

        // Featured Books RecyclerView - Horizontal
        rvFeaturedBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        featuredBooksAdapter = new BookAdapter(featuredBooksList, this::onBookClick);
        rvFeaturedBooks.setAdapter(featuredBooksAdapter);

        // Ranking Books RecyclerView - Vertical
        rvRankingBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        rankingBooksAdapter = new RankingBookAdapter(rankingBooksList, this::onBookClick);
        rvRankingBooks.setAdapter(rankingBooksAdapter);
    }

    private void setupSearchFunctionality() {
        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    // TODO: Implement search functionality
                    Log.d(TAG, "Searching for: " + s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        edSearch.setOnClickListener(v -> {
            // TODO: Navigate to search page
            Toast.makeText(getContext(), "Tìm kiếm sách...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupClickListeners() {
        ivNotification.setOnClickListener(v -> {
            // TODO: Navigate to notifications
            Toast.makeText(getContext(), "Thông báo", Toast.LENGTH_SHORT).show();
        });

        ivCart.setOnClickListener(v -> {
            // Navigate to cart
            navigateToCart();
        });

        ivUserAvatar.setOnClickListener(v -> {
            // TODO: Navigate to profile
            Toast.makeText(getContext(), "Thông tin cá nhân", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadData() {
        loadCategories();
        loadFeaturedBooks();
        loadRankingBooks();
    }

    private void loadCategories() {
        db.collection("bookTypes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookType bookType = document.toObject(BookType.class);
                        bookType.setId(document.getId());
                        categoryList.add(bookType);
                    }
                    categoryAdapter.notifyDataSetChanged();
                    Log.d(TAG, "✅ Loaded " + categoryList.size() + " categories");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading categories", e);
                    Toast.makeText(getContext(), "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFeaturedBooks() {
        db.collection("books")
                .whereGreaterThan("quantity", 0) // Chỉ lấy sách còn hàng
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    featuredBooksList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Book book = document.toObject(Book.class);
                        book.setId(document.getId());
                        featuredBooksList.add(book);
                    }
                    featuredBooksAdapter.notifyDataSetChanged();
                    Log.d(TAG, "✅ Loaded " + featuredBooksList.size() + " featured books");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading featured books", e);
                    Toast.makeText(getContext(), "Lỗi tải sách nổi bật", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRankingBooks() {
        // Load top selling books based on some criteria
        db.collection("books")
                .whereGreaterThan("quantity", 0)
                .orderBy("quantity", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    rankingBooksList.clear();
                    int rank = 1;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Book book = document.toObject(Book.class);
                        book.setId(document.getId());
                        rankingBooksList.add(book);
                        rank++;
                    }
                    rankingBooksAdapter.notifyDataSetChanged();
                    Log.d(TAG, "✅ Loaded " + rankingBooksList.size() + " ranking books");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading ranking books", e);
                    Toast.makeText(getContext(), "Lỗi tải bảng xếp hạng", Toast.LENGTH_SHORT).show();
                });
    }

    // Event handlers
    private void onBannerClick(BannerItem banner) {
        Toast.makeText(getContext(), "Banner clicked: " + banner.getId(), Toast.LENGTH_SHORT).show();
        // TODO: Navigate to banner target page
    }

    private void onCategoryClick(BookType category) {
        // TODO: Navigate to category page with books filter
        Toast.makeText(getContext(), "Danh mục: " + category.getName(), Toast.LENGTH_SHORT).show();

        // Example: Navigate to BookListFragment with category filter
        /*
         * BookListFragment fragment = BookListFragment.newInstance(category.getId(),
         * category.getName());
         * requireActivity().getSupportFragmentManager()
         * .beginTransaction()
         * .replace(R.id.fragmentContainer, fragment)
         * .addToBackStack(null)
         * .commit();
         */
    }

    private void onBookClick(Book book) {
        // Navigate to book detail
        BookDetailFragment fragment = BookDetailFragment.newInstance(book.getId());

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToCart() {
        CartFragment cartFragment = new CartFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, cartFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cleanup handler
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause auto slide when fragment is paused
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume auto slide when fragment is resumed
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }
}