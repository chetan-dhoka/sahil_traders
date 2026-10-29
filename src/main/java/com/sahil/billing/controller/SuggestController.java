package com.sahil.billing.controller;

import com.sahil.billing.repository.BuyerRepository;
import com.sahil.billing.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SuggestController {

    @Autowired private SellerRepository sellerRepo;
    @Autowired private BuyerRepository  buyerRepo;

    @GetMapping("/sellers/suggest")
    public List<String> sellers(@RequestParam String q) {
        return sellerRepo.suggest(q.trim().toLowerCase(), PageRequest.of(0, 10))
                .stream().map(s -> s.getName()).collect(Collectors.toList());
    }

    @GetMapping("/buyers/suggest")
    public List<String> buyers(@RequestParam String q) {
        return buyerRepo.suggest(q.trim().toLowerCase(), PageRequest.of(0, 10))
                .stream().map(b -> b.getName()).collect(Collectors.toList());
    }
}
