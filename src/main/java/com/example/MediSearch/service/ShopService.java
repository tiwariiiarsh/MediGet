package com.example.MediSearch.service;

import com.example.MediSearch.payload.MedicineDTO;
import com.example.MediSearch.payload.ShopDTO;
import com.example.MediSearch.security.response.MessageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ShopService {

    ShopDTO createShop(ShopDTO shopDTO);

    ShopDTO getMyShop();

    ShopDTO updateShop(ShopDTO shopDTO);

    String deleteShop();

    ShopDTO getShopDetails(Long shopId);

    ShopDTO updateShopImage(Long shopId, MultipartFile image) throws IOException;
}