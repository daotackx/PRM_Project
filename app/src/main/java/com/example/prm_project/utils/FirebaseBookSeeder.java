package com.example.prm_project.utils;

import android.util.Log;

import com.example.prm_project.model.Book;
import com.example.prm_project.model.BookType;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FirebaseBookSeeder {
    private static final String TAG = "FirebaseBookSeeder";
    private static final String COLLECTION_BOOKS = "books";
    private final FirebaseFirestore db;

    public FirebaseBookSeeder() {
        db = FirebaseFirestore.getInstance();
    }

    public void seedBooks() {
        List<Book> books = createBookData();

        for (Book book : books) {
            db.collection(COLLECTION_BOOKS)
                .document(book.getId())
                .set(book)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "\u2705 Đã thêm sách: " + book.getName()))
                .addOnFailureListener(e -> Log.e(TAG, "\u274C Lỗi thêm sách: " + book.getName(), e));
        }
    }

    private List<Book> createBookData() {
        List<Book> books = new ArrayList<>();

        BookType vanHoc = new BookType("1", "Văn học");
        BookType thieuNhi = new BookType("2", "Thiếu nhi");
        BookType tieuSu = new BookType("3", "Tiểu sử");
        BookType kyNangSong = new BookType("4", "Kỹ năng sống");
        BookType kinhDi = new BookType("5", "Kinh dị");
        BookType khoaHoc = new BookType("6", "Khoa học");

        // Add all 30 books here, categorized by type

        books.add(new Book("1", "Kinh dị - Bí ẩn", "Nguyễn Nhật Ánh", "Một câu chuyện kinh dị đầy bí ẩn...", 74000, 50, "https://cdn0.fahasa.com/media/catalog/product/c/o/co-the-tu-chua-lanh-3_bia-1.jpg", kinhDi));
        books.add(new Book("2", "Truyện Kinh Dị Việt Nam - Thần Hổ", "TchyA", "Nổi tiếng với thể loại truyền kỳ...", 82000, 35, "https://cdn0.fahasa.com/media/catalog/product/8/9/8935244877243.jpg", kinhDi));
        books.add(new Book("3", "Tiếng Gọi Từ Nửa Đêm", "Shirley Jackson", "Một ngôi nhà ma ám và những bí mật kinh hoàng trong đêm tối.", 68000, 55, "https://cdn0.fahasa.com/media/catalog/product/8/9/8936066708937.jpg", kinhDi));
        books.add(new Book("4", "Hồn Ma Trong Lâu Đài", "Edgar Allan Poe", "Tuyển tập truyện kinh dị ngắn đầy ám ảnh.", 59000, 60, "https://cdn0.fahasa.com/media/catalog/product/8/9/8936066708944.jpg", kinhDi));
        books.add(new Book("5", "100 Truyền Thuyết Đô Thị Kinh Dị Đài Loan", "Dương Hải Ngạn, Tạ Nghi An, Nguyễn Tông Hiến...", "Sau 12 giờ đêm...", 95000, 35, "https://cdn0.fahasa.com/media/catalog/product/b/i/bia-1_100truyenthuyetdothikinhdidailoantap1.jpg", kinhDi));

        books.add(new Book("6", "Chuyện Con Mèo Dạy Hải Âu Bay", "Luis Sepúlveda", "Tình bạn giữa mèo và hải âu...", 65000, 80, "https://cdn0.fahasa.com/media/catalog/product/c/h/chuyen-con-meo-day-hai-au-bay-01.jpg", thieuNhi));
        books.add(new Book("7", "Cậu Bé Rừng Xanh", "Rudyard Kipling", "Hành trình phiêu lưu của Mowgli...", 58000, 60, "https://cdn0.fahasa.com/media/catalog/product/d/a/danh-tac-muon-thuo_cau-be-rung-xanh.jpg", thieuNhi));
        books.add(new Book("8", "Hoàng Tử Bé", "Antoine de Saint-Exupéry", "Cuốn sách nổi tiếng về tình bạn...", 72000, 70, "https://cdn0.fahasa.com/media/catalog/product/8/9/8935325018732-_3_.jpg", thieuNhi));
        books.add(new Book("9", "Matilda", "Roald Dahl", "Cô bé Matilda vượt qua khó khăn...", 68000, 65, "https://cdn0.fahasa.com/media/catalog/product/9/7/9780142410370_3.jpg", thieuNhi));
        books.add(new Book("10", "Charlie và Nhà Máy Chocolate", "Roald Dahl", "Cuộc phiêu lưu kỳ thú của Charlie...", 75000, 50, "https://cdn0.fahasa.com/media/catalog/product/i/m/image_219998.jpg", thieuNhi));

        books.add(new Book("11", "Donald Trump - Khởi nghiệp và Thành công", "Michael Wolff", "Hành trình của Donald Trump...", 120000, 30, "https://cdn0.fahasa.com/media/catalog/product/8/9/8936066695718.jpg", tieuSu));
        books.add(new Book("12", "Tiểu Sử Steve Jobs", "Walter Isaacson", "Cuộc đời Steve Jobs, người sáng lập Apple.", 300000, 25, "https://cdn0.fahasa.com/media/catalog/product/thumbnailframe/product_frame_alpha_book/frame_image_195509_1_40814_1.jpg", tieuSu));
        books.add(new Book("13", "Elon Musk - Người Kiến Tạo Tương Lai", "Ashlee Vance", "Hành trình Elon Musk từ PayPal đến Tesla.", 140000, 20, "https://cdn0.fahasa.com/media/catalog/product/8/9/8935251419016_1.jpg", tieuSu));
        books.add(new Book("14", "Tôi Là Tôi", "Drew Canole", "Bạn biết mình phải thay đổi nhưng không biết bắt đầu từ đâu...", 88000, 45, "https://cdn0.fahasa.com/media/catalog/product/8/9/8935074130730.jpg", tieuSu));
        books.add(new Book("15", "Nhật Ký Anne Frank", "Anne Frank", "Hồi ký cảm động của cô bé trong Thế chiến II.", 95000, 40, "https://cdn0.fahasa.com/media/catalog/product/8/9/8936066709002.jpg", tieuSu));

        books.add(new Book("16", "Take Note! Ngắn Gọn", "Hoàng Anh Tú", "Hướng dẫn ghi chú hiệu quả, ngắn gọn.", 85000, 75, "https://cdn0.fahasa.com/media/catalog/product/8/9/8936066708920.jpg", kyNangSong));
        books.add(new Book("17", "Đắc Nhân Tâm", "Dale Carnegie", "Cuốn sách kinh điển về giao tiếp.", 90000, 100, "https://cdn0.fahasa.com/media/catalog/product/8/9/8935270704704.jpg", kyNangSong));
        books.add(new Book("18", "7 Thói Quen Hiệu Quả", "Stephen R. Covey", "Thói quen để đạt thành công cá nhân.", 110000, 85, "https://cdn0.fahasa.com/media/catalog/product/8/9/8935280400733.jpg", kyNangSong));
        books.add(new Book("19", "Sức Mạnh Của Thói Quen", "Charles Duhigg", "Khám phá cách thói quen hình thành.", 98000, 60, "https://cdn0.fahasa.com/media/catalog/product/8/9/8935251416916.jpg", kyNangSong));
        books.add(new Book("20", "Tư Duy Nhanh Và Chậm", "Daniel Kahneman", "Cách con người đưa ra quyết định.", 125000, 50, "https://cdn0.fahasa.com/media/catalog/product/8/9/8936066709026.jpg", kyNangSong));

        books.add(new Book("21", "Nhà Giả Kim", "Paulo Coelho", "Hành trình tìm kiếm ước mơ của chàng trai chăn cừu.", 78000, 45, "https://cdn0.fahasa.com/media/catalog/product/i/m/image_195509_1_36793.jpg", vanHoc));
        books.add(new Book("22", "Ông Trăm Tuổi Trèo Qua Cửa Sổ Và Biến Mất", "Jonas Jonasson", "Hành trình hài hước và bất ngờ.", 92000, 55, "https://cdn0.fahasa.com/media/catalog/product/8/9/8936066709033.jpg", vanHoc));
        books.add(new Book("23", "Rừng Na Uy", "Haruki Murakami", "Câu chuyện tình yêu trong Nhật Bản hiện đại.", 105000, 40, "https://cdn0.fahasa.com/media/catalog/product/8/9/8936066709040.jpg", vanHoc));
        books.add(new Book("24", "Những Người Khốn Khổ", "Victor Hugo", "Miêu tả cuộc sống nghèo khổ ở Pháp thế kỷ 19.", 200000, 15, "https://cdn0.fahasa.com/media/catalog/product/8/9/8935086840252.jpg", vanHoc));
        books.add(new Book("25", "Người Đua Diều", "Khaled Hosseini", "Tình bạn và sự chuộc lỗi tại Afghanistan.", 87000, 50, "https://cdn0.fahasa.com/media/catalog/product/8/9/8936066709064.jpg", vanHoc));

        books.add(new Book("26", "Luật Của Một - Vũ Trụ Trong Mỗi Chúng Ta", "Carla L. Rueckert", "Bí ẩn của vũ trụ từ góc nhìn khoa học.", 135000, 35, "https://cdn0.fahasa.com/media/catalog/product/b/i/bia-luat-cua-mot-gui-in-1.jpg", khoaHoc));
        books.add(new Book("27", "Lược Sử Thời Gian", "Stephen Hawking", "Giải thích các khái niệm phức tạp về vũ trụ.", 115000, 40, "https://cdn0.fahasa.com/media/catalog/product/8/9/8936066693882.jpg", khoaHoc));
        books.add(new Book("28", "Nguồn Gốc Các Loài", "Charles Darwin", "Thuyết tiến hóa và sự phát triển của các loài.", 145000, 30, "https://cdn0.fahasa.com/media/catalog/product/8/9/8936066709088.jpg", khoaHoc));
        books.add(new Book("29", "Mãi Mãi Là Bí Ẩn", "Nhiều Tác Giả", "Khám phá vũ trụ qua góc nhìn vật lý thiên văn.", 130000, 45, "https://cdn0.fahasa.com/media/catalog/product/8/9/8931805100188.jpg", khoaHoc));
        books.add(new Book("30", "Khoa Học Về Yoga", "Ann Swanson", "Các bài tập thở và thiền theo khoa học.", 125000, 50, "https://cdn0.fahasa.com/media/catalog/product/k/h/khoa-hoc-ve-yoga-04.jpg", khoaHoc));

        return books;
    }
}
