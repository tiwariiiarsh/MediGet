package com.example.MediSearch.service;

import com.example.MediSearch.payload.ShopDTO;
import com.example.MediSearch.security.response.MessageResponse;

public interface ShopService {

    ShopDTO createShop(ShopDTO shopDTO);

    ShopDTO getMyShop();

    ShopDTO updateShop(ShopDTO shopDTO);

    String deleteShop();
}