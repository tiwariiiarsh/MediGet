package com.example.MediSearch.service;

import com.example.MediSearch.Utils.AuthUtils;
import com.example.MediSearch.exceptions.ApiException;
import com.example.MediSearch.exceptions.ResourceNotFoundException;
import com.example.MediSearch.model.*;
import com.example.MediSearch.payload.BillResponseDTO;
import com.example.MediSearch.payload.CartItemDTO;
import com.example.MediSearch.repository.BillRepository;
import com.example.MediSearch.repository.MedicineRepository;
import com.example.MediSearch.repository.ShopRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    private Shop validateSeller(Long shopId) {

        User seller = authUtils.loggedInUser();

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shop","shopId",shopId));

        if(!shop.getSeller().getUserId()
                .equals(seller.getUserId()))
            throw new ApiException("Unauthorized");

        return shop;
    }

    @Override
    public BillResponseDTO generateBill(
            Long shopId,
            List<CartItemDTO> cartItems) {

        Shop shop = validateSeller(shopId);

        Bill bill = new Bill();
        bill.setShop(shop);
        bill.setBillDate(LocalDate.now());

        double total = 0;

        for(CartItemDTO item : cartItems) {

            Medicine medicine =
                    medicineRepository.findById(
                                    item.getMedicineId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Medicine","id",
                                            item.getMedicineId()));

            if(medicine.getQuantity() < item.getQuantity())
                throw new ApiException("Not enough stock");

            medicine.setQuantity(
                    medicine.getQuantity()
                            - item.getQuantity());

            BillItem billItem = new BillItem();
            billItem.setBill(bill);
            billItem.setMedicine(medicine);
            billItem.setQuantity(item.getQuantity());
            billItem.setPriceAtSale(
                    medicine.getSpecialPrice());

            bill.getItems().add(billItem);

            total += medicine.getSpecialPrice()
                    * item.getQuantity();
        }

        bill.setTotalAmount(total);

        Bill savedBill =
                billRepository.save(bill);

        BillResponseDTO response =
                new BillResponseDTO();

        response.setBillId(savedBill.getBillId());
        response.setShopName(shop.getShopName());
        response.setBillDate(savedBill.getBillDate());
        response.setTotalAmount(savedBill.getTotalAmount());

        return response;
    }

    @Override
    public String getBestSelling(Long shopId) {

        List<Object[]> result =
                billRepository.findMonthlySales(shopId);

        if(result.isEmpty())
            return "No sales this month";

        return "Best Selling: "
                + result.get(0)[0]
                + " Sold: "
                + result.get(0)[1];
    }

    @Override
    public String getLeastSelling(Long shopId) {

        List<Object[]> result =
                billRepository.findMonthlySales(shopId);

        if(result.isEmpty())
            return "No sales";

        Object[] last =
                result.get(result.size() - 1);

        return "Least Selling: "
                + last[0]
                + " Sold: "
                + last[1];
    }
}