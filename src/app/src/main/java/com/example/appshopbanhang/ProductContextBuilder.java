package com.example.appshopbanhang;

import android.util.Log;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ProductContextBuilder {

    private static final int MAX_PRODUCTS = 25;

    public ArrayList<SanPham> getRelevantProducts(String userQuery, DatabaseHelper db) {

        String[] keywords = extractKeywords(userQuery);

        ArrayList<SanPham> allProducts = new ArrayList<>();

        for (String keyword : keywords) {
            ArrayList<SanPham> products = db.searchSanPhamByName(keyword);
            for (SanPham product : products) {

                if (!containsProduct(allProducts, product)) {
                    allProducts.add(product);
                }
            }
        }

        if (allProducts.size() > MAX_PRODUCTS) {
            return new ArrayList<>(allProducts.subList(0, MAX_PRODUCTS));
        }

        return allProducts;
    }

    private String[] extractKeywords(String query) {
        String[] stopWords = {"giá", "bao", "nhiêu", "là", "gì", "có", "không", "tôi", "muốn", "cần", "cho", "về", "của", "được", "sản", "phẩm"};

        String cleanQuery = query.toLowerCase();
        for (String stopWord : stopWords) {
            cleanQuery = cleanQuery.replace(stopWord, " ");
        }

        String[] words = cleanQuery.trim().split("\\s+");
        ArrayList<String> keywords = new ArrayList<>();

        for (String word : words) {
            if (word.length() > 1) {
                keywords.add(word.trim());
            }
        }

        if (keywords.isEmpty()) {
            return new String[]{query};
        }

        return keywords.toArray(new String[0]);
    }


    private boolean containsProduct(ArrayList<SanPham> list, SanPham product) {
        for (SanPham p : list) {
            if (p.getMasp().equals(product.getMasp())) {
                return true;
            }
        }
        return false;
    }


    public String buildProductContext(ArrayList<SanPham> products) {
        if (products == null || products.isEmpty()) {
            return "Hiện tại không tìm thấy sản phẩm phù hợp trong kho.";
        }

        StringBuilder context = new StringBuilder();
        context.append("=== DANH SÁCH SẢN PHẨM CÓ SẴN ===\n\n");

        for (int i = 0; i < products.size(); i++) {
            SanPham sp = products.get(i);

            context.append((i + 1)).append(". ");
            context.append("Tên: ").append(sp.getTensp()).append("\n");
            context.append("   Giá: ").append(formatPrice(sp.getDongia())).append("\n");
            context.append("   Tồn kho: ").append(sp.getSoluongkho()).append(" sản phẩm");

            if (sp.getSoluongkho() == 0) {
                context.append(" (HẾT HÀNG)");
            } else if (sp.getSoluongkho() < 5) {
                context.append(" (Sắp hết)");
            }

            context.append("\n");

            if (sp.getMota() != null && !sp.getMota().trim().isEmpty()) {
                String mota = sp.getMota();
                if (mota.length() > 100) {
                    mota = mota.substring(0, 100) + "...";
                }
                context.append("   Mô tả: ").append(mota).append("\n");
            }

            context.append("\n");
        }

        return context.toString();
    }

    private String formatPrice(Float price) {
        if (price == null) return "Liên hệ";

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }


    public String buildFullPrompt(String userQuery, String productContext) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("=== VAI TRÒ CỦA BẠN ===\n");
        prompt.append("Bạn là nhân viên bán hàng chuyên nghiệp, thân thiện và nhiệt tình.\n");
        prompt.append("Nhiệm vụ: Tư vấn sản phẩm, trả lời về giá cả, tính năng, so sánh sản phẩm.\n\n");

        prompt.append("=== HƯỚNG DẪN ===\n");
        prompt.append("- Luôn dựa vào danh sách sản phẩm bên dưới để đưa ra câu trả lời chính xác.\n");
        prompt.append("- Nếu sản phẩm hết hàng, hãy gợi ý sản phẩm tương tự.\n");
        prompt.append("- Trả lời ngắn gọn, súc tích, dễ hiểu, thân thiện.\n");
        prompt.append("- Nếu không có sản phẩm phù hợp, hãy lịch sự thông báo và gợi ý khác.\n");
        prompt.append("- Không bịa đặt thông tin về sản phẩm không có trong danh sách.\n\n");

        prompt.append(productContext).append("\n");

        prompt.append("=== CÂU HỎI CỦA KHÁCH HÀNG ===\n");
        prompt.append(userQuery).append("\n\n");

        prompt.append("Hãy trả lời câu hỏi của khách hàng dựa trên thông tin sản phẩm ở trên:");

        return prompt.toString();
    }
}
