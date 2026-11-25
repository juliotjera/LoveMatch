package com.esmia.lovematch.mapper;

import com.esmia.lovematch.dto.response.MessageResponse;
import com.esmia.lovematch.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageResponse reponse(Message m) {
        return new MessageResponse(m.getId(), m.getCorrespondance().getId(), m.getAuteur().getId(), m.getContenu(),
            m.getEnvoyeLe(), m.getLuLe());
    }
}
