package com.hilbert.wallet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hilbert.wallet.dto.WalletItemDTO;
import com.hilbert.wallet.entity.Wallet;
import com.hilbert.wallet.entity.WalletItem;
import com.hilbert.wallet.service.WalletItemService;
import com.hilbert.wallet.util.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class WalletItemControllerTest {
    @MockBean
    WalletItemService service;

    @Autowired
    MockMvc mvc;

    private static final Long ID = 1L;
    private static final Date DATE = new Date();
    private static final LocalDate TODAY = LocalDate.now();
    private static final Type TYPE = Type.EN;
    private static final String DESCRIPTION = "Conta de Luz";
    private static final BigDecimal VALUE = BigDecimal.valueOf(65);
    private static final String URL = "/wallet-item";

    @Test
    public void testSave() throws Exception {

        BDDMockito.given(service.save(Mockito.any(WalletItem.class))).willReturn(getMockWalletItem());

        mvc.perform(MockMvcRequestBuilders.post(URL).content(getJsonPayload())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(ID))
                .andExpect(jsonPath("$.data.date").value(TODAY.format(getDateFormater())))
                .andExpect(jsonPath("$.data.description").value(DESCRIPTION))
                .andExpect(jsonPath("$.data.type").value(TYPE.getValue()))
                .andExpect(jsonPath("$.data.value").value(VALUE))
                .andExpect(jsonPath("$.data.wallet").value(ID));

    }

    @Test
    public void testFindBetweenDates() throws Exception {
        List<WalletItem> list = new ArrayList<>();
        list.add(getMockWalletItem());
        Page<WalletItem> page = new PageImpl(list);

        String startDate = TODAY.format(getDateFormater());
        String endDate = TODAY.plusDays(5).format(getDateFormater());

        BDDMockito.given(service.findBetweenDates(Mockito.anyLong(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt())).willReturn(page);

        mvc.perform(MockMvcRequestBuilders.get(URL + "/1?startDate=" + startDate + "&endDate=" + endDate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(ID))
                .andExpect(jsonPath("$.data.content[0].date").value(TODAY.format(getDateFormater())))
                .andExpect(jsonPath("$.data.content[0].description").value(DESCRIPTION))
                .andExpect(jsonPath("$.data.content[0].type").value(TYPE.getValue()))
                .andExpect(jsonPath("$.data.content[0].value").value(VALUE))
                .andExpect(jsonPath("$.data.content[0].wallet").value(ID));

    }

    @Test
    public void testFindByType() throws Exception {
        List<WalletItem> list = new ArrayList<>();
        list.add(getMockWalletItem());

        BDDMockito.given(service.findByWalletAndType(Mockito.anyLong(), Mockito.any(Type.class))).willReturn(list);

        mvc.perform(MockMvcRequestBuilders.get(URL+"/type/1?type=ENTRADA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.[0].id").value(ID))
                .andExpect(jsonPath("$.data.[0].date").value(TODAY.format(getDateFormater())))
                .andExpect(jsonPath("$.data.[0].description").value(DESCRIPTION))
                .andExpect(jsonPath("$.data.[0].type").value(TYPE.getValue()))
                .andExpect(jsonPath("$.data.[0].value").value(VALUE))
                .andExpect(jsonPath("$.data.[0].wallet").value(ID));

    }

    @Test
    public void testSumByWallet() throws Exception {
        BigDecimal value = BigDecimal.valueOf(536.90);

        BDDMockito.given(service.sumByWalletId(Mockito.anyLong())).willReturn(value);

        mvc.perform(MockMvcRequestBuilders.get(URL+"/total/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("536.9"));

    }

    @Test
    public void testUpdate() throws Exception {

        String description = "Nova descrição";
        Wallet w = new Wallet();
        w.setId(ID);

        BDDMockito.given(service.findById(Mockito.anyLong())).willReturn(Optional.of(getMockWalletItem()));
        BDDMockito.given(service.save(Mockito.any(WalletItem.class))).willReturn(new WalletItem(1L, w, DATE, Type.SD, description, VALUE));

        mvc.perform(MockMvcRequestBuilders.put(URL).content(getJsonPayload())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(ID))
                .andExpect(jsonPath("$.data.date").value(TODAY.format(getDateFormater())))
                .andExpect(jsonPath("$.data.description").value(description))
                .andExpect(jsonPath("$.data.type").value(Type.SD.getValue()))
                .andExpect(jsonPath("$.data.value").value(VALUE))
                .andExpect(jsonPath("$.data.wallet").value(ID));

    }

    @Test
    public void testUpdateWalletChange() throws Exception {

        Wallet w = new Wallet();
        w.setId(99L);

        WalletItem wi = new WalletItem(1L, w, DATE, Type.SD, DESCRIPTION, VALUE);

        BDDMockito.given(service.findById(Mockito.anyLong())).willReturn(Optional.of(wi));

        mvc.perform(MockMvcRequestBuilders.put(URL).content(getJsonPayload())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0]").value("Você não pode alterar a carteira"));

    }

    @Test
    public void testUpdateInvalidId() throws Exception {

        BDDMockito.given(service.findById(Mockito.anyLong())).willReturn(Optional.empty());

        mvc.perform(MockMvcRequestBuilders.put(URL).content(getJsonPayload())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0]").value("WalletItem não encontrado"));

    }

    @Test
    public void testDelete() throws JsonProcessingException, Exception {

        BDDMockito.given(service.findById(Mockito.anyLong())).willReturn(Optional.of(new WalletItem()));

        mvc.perform(MockMvcRequestBuilders.delete(URL+"/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("WalletItem de id "+ ID +" apagada com sucesso"));
    }

    @Test
    public void testDeleteInvalid() throws Exception {

        BDDMockito.given(service.findById(Mockito.anyLong())).willReturn(Optional.empty());

        mvc.perform(MockMvcRequestBuilders.delete(URL+"/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0]").value("WalletItem de id "+ 99 + " não encontrada"));

    }

    private WalletItem getMockWalletItem() {
        Wallet w = new Wallet();
        w.setId(1L);

        WalletItem wi = new WalletItem(1L, w, DATE, TYPE, DESCRIPTION, VALUE);
        return wi;
    }

    public String getJsonPayload() throws JsonProcessingException {
        WalletItemDTO dto = new WalletItemDTO();
        dto.setId(ID);
        dto.setDate(DATE);
        dto.setDescription(DESCRIPTION);
        dto.setType(TYPE.getValue());
        dto.setValue(VALUE);
        dto.setWallet(ID);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(dto);
    }

    private DateTimeFormatter getDateFormater() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return formatter;
    }

}
