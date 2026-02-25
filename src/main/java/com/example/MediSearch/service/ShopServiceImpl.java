package com.example.MediSearch.service;

import com.example.MediSearch.Utils.AuthUtils;
import com.example.MediSearch.exceptions.ApiException;
import com.example.MediSearch.exceptions.ResourceNotFoundException;
import com.example.MediSearch.model.Medicine;
import com.example.MediSearch.model.Shop;
import com.example.MediSearch.model.User;
import com.example.MediSearch.payload.MedicineDTO;
import com.example.MediSearch.payload.ShopDTO;
import com.example.MediSearch.repository.MedicineRepository;
import com.example.MediSearch.repository.ShopRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private ModelMapper modelMapper;



    @Autowired
    private MedicineRepository medicineRepository;

    // ================= CREATE SHOP =================

    @Override
    public ShopDTO createShop(ShopDTO shopDTO) {

        User seller = authUtils.loggedInUser();

        if (shopRepository.existsById(seller.getUserId())) {
            throw new ApiException("Shop already exists for this seller");
        }

        Shop shop = modelMapper.map(shopDTO, Shop.class);
        shop.setSeller(seller);

        Shop savedShop = shopRepository.save(shop);

        return modelMapper.map(savedShop, ShopDTO.class);
    }

    // ================= GET MY SHOP =================

    @Override
    public ShopDTO getMyShop() {

        User seller = authUtils.loggedInUser();

        Shop shop = shopRepository.findById(seller.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shop", "shopId", seller.getUserId()));

        return modelMapper.map(shop, ShopDTO.class);
    }

    // ================= UPDATE SHOP =================

    @Override
    public ShopDTO updateShop(ShopDTO shopDTO) {

        User seller = authUtils.loggedInUser();

        Shop shop = shopRepository.findById(seller.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shop", "shopId", seller.getUserId()));

        modelMapper.map(shopDTO, shop);

        Shop updatedShop = shopRepository.save(shop);

        return modelMapper.map(updatedShop, ShopDTO.class);
    }

    // ================= DELETE SHOP =================

    @Override
    public String deleteShop() {

        User seller = authUtils.loggedInUser();

        Shop shop = shopRepository.findById(seller.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shop", "shopId", seller.getUserId()));

        shopRepository.delete(shop);

        return "Shop deleted successfully";
    }

    @Override
    public ShopDTO getShopDetails(Long shopId) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        ShopDTO dto = new ShopDTO();

        dto.setShopId(shop.getShopId());
        dto.setShopName(shop.getShopName());
        dto.setBuildingName(shop.getBuildingName());
        dto.setStreet(shop.getStreet());
        dto.setCity(shop.getCity());
        dto.setState(shop.getState());
        dto.setCountry(shop.getCountry());
        dto.setPincode(shop.getPincode());
        dto.setLatitude(shop.getLatitude());
        dto.setLongitude(shop.getLongitude());
        dto.setOpen(shop.getIsOpen());

        List<MedicineDTO> medicines = shop.getMedicines().stream()
                .map(med -> {
                    MedicineDTO m = new MedicineDTO();
                    m.setMedicineId(med.getMedicineId());
                    m.setMedicineName(med.getMedicineName());
                    m.setDescription(med.getDescription());
                    m.setQuantity(med.getQuantity());
                    m.setPrice(med.getPrice());
                    m.setDiscount(med.getDiscount());
                    m.setSpecialPrice(med.getSpecialPrice());
                    m.setImage(med.getImage());
                    m.setShopId(med.getShop().getShopId());
                    m.setShopName(shop.getShopName());
                    m.setShopCity(shop.getCity());

                    return m;
                }).toList();

        dto.setMedicines(medicines);

        return dto;
    }
}