package com.example.MediSearch.service;

import com.example.MediSearch.payload.ShopDTO;

public interface ShopService {

    ShopDTO createShop(ShopDTO shopDTO);

    ShopDTO getMyShop();

    ShopDTO updateShop(ShopDTO shopDTO);

    String deleteShop();
}