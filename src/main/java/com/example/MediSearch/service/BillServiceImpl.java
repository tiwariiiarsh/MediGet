package com.example.MediSearch.service;

import com.example.MediSearch.Utils.AuthUtils;
import com.example.MediSearch.exceptions.ApiException;
import com.example.MediSearch.exceptions.ResourceNotFoundException;
import com.example.MediSearch.model.*;
import com.example.MediSearch.payload.BillItemResponseDTO;
import com.example.MediSearch.payload.BillResponseDTO;
import com.example.MediSearch.payload.CartItemDTO;
import com.example.MediSearch.repository.BillRepository;
import com.example.MediSearch.repository.MedicineRepository;
import com.example.MediSearch.repository.ShopRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class BillServiceImpl implements BillService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private AuthUtils authUtils;

    // ================= VALIDATE SELLER =================

    private Shop validateSeller(Long shopId) {

        User seller = authUtils.loggedInUser();

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shop", "shopId", shopId));

        if (!shop.getSeller().getUserId()
                .equals(seller.getUserId()))
            throw new ApiException("Unauthorized");

        return shop;
    }

    // ================= GENERATE BILL =================

    @Override
    public BillResponseDTO generateBill(
            Long shopId,
            List<CartItemDTO> cartItems) {

        Shop shop = validateSeller(shopId);

        if (cartItems == null || cartItems.isEmpty())
            throw new ApiException("Cart cannot be empty");

        Bill bill = new Bill();
        bill.setShop(shop);
        bill.setBillDate(LocalDate.now());

        List<BillItem> billItems = new ArrayList<>();

        double totalAmount = 0.0;

        for (CartItemDTO item : cartItems) {

            Medicine medicine =
                    medicineRepository.findById(item.getMedicineId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Medicine", "id",
                                            item.getMedicineId()));

            if (medicine.getQuantity() == null
                    || medicine.getQuantity() < item.getQuantity())
                throw new ApiException(
                        "Not enough stock for "
                                + medicine.getMedicineName());

            // 🔥 Reduce Stock
            medicine.setQuantity(
                    medicine.getQuantity()
                            - item.getQuantity());

            // In generateBill(), after reducing stock, ADD:
            medicine.setSalesCount(
                    (medicine.getSalesCount() == null ? 0L : medicine.getSalesCount())
                            + item.getQuantity()
            );

            medicineRepository.save(medicine);

            BillItem billItem = new BillItem();
            billItem.setBill(bill);  // VERY IMPORTANT
            billItem.setMedicine(medicine);
            billItem.setQuantity(item.getQuantity());
//            billItem.setPrice(
//                    medicine.getSpecialPrice());
            Double price = medicine.getSpecialPrice() != null
                    ? medicine.getSpecialPrice()
                    : medicine.getPrice();

            billItem.setPrice(price);

            double itemTotal =
                    item.getQuantity()
                            * medicine.getSpecialPrice();

            billItem.setTotalPrice(itemTotal);

            totalAmount += itemTotal;

            billItems.add(billItem);
        }

        bill.setItems(billItems);  // VERY IMPORTANT
        bill.setTotalAmount(totalAmount);

        Bill savedBill =
                billRepository.save(bill);

        return convertToResponse(savedBill);
    }

    // ================= CONVERT TO RESPONSE =================

    private BillResponseDTO convertToResponse(Bill bill) {

        BillResponseDTO response =
                new BillResponseDTO();

        response.setBillId(bill.getBillId());
        response.setShopName(
                bill.getShop().getShopName());
        response.setBillDate(bill.getBillDate());
        response.setTotalAmount(
                bill.getTotalAmount());

        List<BillItemResponseDTO> itemResponses =
                bill.getItems().stream().map(item -> {

                    BillItemResponseDTO dto =
                            new BillItemResponseDTO();

                    dto.setMedicineName(
                            item.getMedicine()
                                    .getMedicineName());
                    dto.setQuantity(item.getQuantity());
                    dto.setPrice(item.getPrice());
                    dto.setTotalPrice(
                            item.getTotalPrice());

                    return dto;

                }).toList();

        response.setItems(itemResponses);

        return response;
    }

    // ================= BEST SELLING =================

    @Override
    public String getBestSelling(Long shopId) {

        List<Object[]> result =
                billRepository.findMonthlySales(shopId);

        if (result.isEmpty())
            return "No sales this month";

        return "Best Selling: "
                + result.get(0)[0]
                + " Sold: "
                + result.get(0)[1];
    }

    // ================= LEAST SELLING =================

    @Override
    public String getLeastSelling(Long shopId) {

        List<Object[]> result =
                billRepository.findMonthlySales(shopId);

        if (result.isEmpty())
            return "No sales this month";

        Object[] last =
                result.get(result.size() - 1);

        return "Least Selling: "
                + last[0]
                + " Sold: "
                + last[1];
    }
}