package com.example.JWTImplemenation.Controller;

import com.example.JWTImplemenation.DTO.FeedbackDTO;
import com.example.JWTImplemenation.Service.IService.IFeedbackservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    @Autowired
    private IFeedbackservice feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackDTO> createFeedback(@RequestBody FeedbackDTO feedbackDTO) {
        return feedbackService.createFeedback(feedbackDTO);
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<FeedbackDTO> updateFeedback(@PathVariable Integer feedbackId,
            @RequestBody FeedbackDTO feedbackDTO) {
        return feedbackService.updateFeedback(feedbackId, feedbackDTO);
    }

    @GetMapping("/{orderItemId}")
    public ResponseEntity<FeedbackDTO> getFeedback(@PathVariable("orderItemId") Integer orderItemId) {
        return feedbackService.getFeedbackByProductAndOrder(orderItemId);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<FeedbackDTO>> getAllFeedbackByProduct(
            @PathVariable Integer productId) {
        return feedbackService.getAllFeedbackByProduct(productId);
    }
}
