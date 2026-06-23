package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.ChatMessageResponse;
import com.urmyfood.backend.application.dto.ChatSessionResponse;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.ChatMessage;
import com.urmyfood.backend.domain.model.ChatSession;
import com.urmyfood.backend.domain.model.ShopProfile;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.ChatMessageRepository;
import com.urmyfood.backend.domain.repository.ChatSessionRepository;
import com.urmyfood.backend.domain.repository.ShopProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AccountRepository accountRepository;
    private final ShopProfileRepository shopProfileRepository;

    @Transactional
    public ChatSessionResponse getOrCreateSession(Long shopId, Long userId) {
        ChatSession session = chatSessionRepository.findByShopIdAndUserId(shopId, userId)
                .orElseGet(() -> chatSessionRepository.save(
                        ChatSession.builder().shopId(shopId).userId(userId).build()
                ));
        return toSessionResponse(session, userId);
    }

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> getSessionsByShop(Long shopId) {
        return chatSessionRepository.findByShopId(shopId).stream()
                .map(s -> toSessionResponse(s, shopId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> getSessionsByUser(Long userId) {
        return chatSessionRepository.findByUserId(userId).stream()
                .map(s -> toSessionResponse(s, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long sessionId, Long requesterId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        if (!session.getShopId().equals(requesterId) && !session.getUserId().equals(requesterId)) {
            throw new RuntimeException("Access denied");
        }
        return chatMessageRepository.findBySessionId(sessionId).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long sessionId, Long senderId, String content, String senderRole, String messageType, String imageUrl) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        if (!session.getShopId().equals(senderId) && !session.getUserId().equals(senderId)) {
            throw new RuntimeException("Access denied: sender is not a participant of this session");
        }
        String type = messageType != null ? messageType : "TEXT";
        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .sessionId(sessionId)
                .senderId(senderId)
                .senderRole(senderRole)
                .messageType(type)
                .content(content != null ? content : "")
                .imageUrl(imageUrl)
                .isRead(false)
                .build());
        chatSessionRepository.touchUpdatedAt(sessionId);
        return toMessageResponse(saved);
    }

    @Transactional
    public void markAsRead(Long sessionId, Long readerId) {
        chatMessageRepository.markAllAsRead(sessionId, readerId);
    }

    private ChatSessionResponse toSessionResponse(ChatSession session, Long viewerId) {
        Account shop = accountRepository.findById(session.getShopId()).orElse(null);
        Account user = accountRepository.findById(session.getUserId()).orElse(null);
        ChatMessage last = chatMessageRepository.findLastBySessionId(session.getId()).orElse(null);
        long unread = chatMessageRepository.countUnread(session.getId(), viewerId);

        return ChatSessionResponse.builder()
                .id(session.getId())
                .shopId(session.getShopId())
                .shopName(shop != null ? shop.getFullName() : "")
                .shopAvatarUrl(resolveShopAvatarUrl(session.getShopId(), shop))
                .userId(session.getUserId())
                .customerName(user != null ? user.getFullName() : "")
                .customerAvatarUrl(user != null ? user.getAvatarUrl() : null)
                .lastMessage(last != null ? last.getContent() : null)
                .lastMessageAt(last != null && last.getSentAt() != null ? last.getSentAt().format(FORMATTER) : null)
                .unreadCount((int) unread)
                .build();
    }

    private String resolveShopAvatarUrl(Long shopId, Account shop) {
        String logoUrl = shopProfileRepository.findByShopId(shopId)
                .map(ShopProfile::getLogoUrl)
                .filter(url -> url != null && !url.isBlank())
                .orElse(null);
        return logoUrl != null ? logoUrl : (shop != null ? shop.getAvatarUrl() : null);
    }

    private ChatMessageResponse toMessageResponse(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .sessionId(msg.getSessionId())
                .senderId(msg.getSenderId())
                .senderRole(msg.getSenderRole())
                .messageType(msg.getMessageType())
                .content(msg.getContent())
                .imageUrl(msg.getImageUrl())
                .read(msg.isRead())
                .sentAt(msg.getSentAt() != null ? msg.getSentAt().format(FORMATTER) : null)
                .build();
    }
}
