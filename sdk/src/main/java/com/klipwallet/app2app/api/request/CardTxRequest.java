package com.klipwallet.app2app.api.request;

import org.json.JSONException;
import org.json.JSONObject;

import com.klipwallet.app2app.api.KlipProtocol;

/**
 * Card 전송 트랜잭션 요청 정보
 */
public class CardTxRequest implements KlipRequest {
    private String contract;
    private String to;
    private String cardId;
    private String from;

    public static class Builder {
        private String contract;
        private String to;
        private String cardId;
        private String from;

        public Builder() {
        }

        /**
         * 전송할 Card 컨트랙트 주소를 입력한다.
         * @param val Card Contract Address
         * @return CardTxRequest.Builder
         */
        public Builder contract(String val) {
            contract = val;
            return this;
        }

        /**
         * 전송받을 EOA 주소를 입력한다.
         * @param val EOA
         * @return CardTxRequest.Builder
         */
        public Builder to(String val) {
            to = val;
            return this;
        }

        /**
         * 전송할 Card ID를 입력한다.
         * @param val Card Id
         * @return CardTxRequest.Builder
         */
        public Builder cardId(String val) {
            cardId = val;
            return this;
        }

        /**
         * (optional) 전송할 EOA 주소를 입력한다.
         * @param val EOA
         * @return CardTxRequest.Builder
         */
        public Builder from(String val) {
            from = val;
            return this;
        }

        public CardTxRequest build() {
            return new CardTxRequest(this);
        }
    }

    public CardTxRequest(Builder builder) {
        contract = builder.contract;
        to = builder.to;
        cardId = builder.cardId;
        from = builder.from;
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put(KlipProtocol.CONTRACT, contract);
            obj.put(KlipProtocol.TO, to);
            obj.put(KlipProtocol.CARD_ID, cardId);
            obj.put(KlipProtocol.FROM, from);
            return obj;
        } catch (JSONException e) {
            throw new IllegalArgumentException("JSON parsing error. Malformed parameters were provided. detailed error message: " + e.toString());
        }
    }
}
