package com.hilbert.wallet.repository;

import com.hilbert.wallet.entity.Wallet;
import com.hilbert.wallet.entity.WalletItem;
import com.hilbert.wallet.util.Type;
import org.checkerframework.checker.nullness.Opt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class WalletItemRepositoryTest {
    private static final Date DATE = new Date();
    private static final Type TYPE = Type.EN;
    private static final String DESCRIPTION = "Conta de luz";
    private static final BigDecimal VALUE= BigDecimal.valueOf(50);
    private Long savedWalletItemId = null;
    private Long savedWalletId = null;

    @Autowired
    WalletItemRepository walletItemRepository;

    @Autowired
    WalletRepository walletRepository;

    @Before
    public void setup(){
        Wallet w = new Wallet();
        w.setName("Carteira Test");
        w.setValue(BigDecimal.valueOf(250));
        walletRepository.save(w);

        WalletItem wi = new WalletItem(null, w, DATE, TYPE, DESCRIPTION, VALUE);
        walletItemRepository.save(wi);

        savedWalletItemId = wi.getId();
        savedWalletId = w.getId();
    }

    @After
    public void tearDown() {
        walletItemRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    public void testSave(){
        Wallet w = new Wallet();
        w.setName("Carteira 1");
        w.setValue(BigDecimal.valueOf(500));

        walletRepository.save(w);

        WalletItem wi = new WalletItem(1L, w, DATE, TYPE, DESCRIPTION, VALUE);
        WalletItem response = walletItemRepository.save(wi);

        assertNotNull(response);
        assertEquals(response.getDescription(), DESCRIPTION);
        assertEquals(response.getType(), TYPE);
        assertEquals(response.getValue(), VALUE);
        assertEquals(response.getType(), TYPE);
        assertEquals(response.getWallet().getId(), w.getId());
    }

    @Test(expected = ConstraintViolationException.class)
    public void testSaveInvalidWalletItem(){
        WalletItem wi = new WalletItem(null, null, DATE, null, DESCRIPTION, null);
        walletItemRepository.save(wi);
    }

    @Test
    public void testUpdate(){
        Optional<WalletItem> wi = walletItemRepository.findById(savedWalletItemId);

        String description = "Descrição alterada";

        WalletItem changed = wi.orElse(null);

        assert changed != null;

        changed.setDescription(description);

        walletItemRepository.save(changed);

        Optional<WalletItem> newWalletItem = walletItemRepository.findById(savedWalletItemId);

        assert newWalletItem.isPresent();

        assertEquals(description, newWalletItem.get().getDescription());
    }

    @Test
    public void deleteWalletItem(){
        Optional<Wallet> wallet = walletRepository.findById(savedWalletId);
        assert wallet.isPresent();

        WalletItem wi = new WalletItem(null, wallet.get(), DATE, TYPE, DESCRIPTION, VALUE);

        walletItemRepository.save(wi);

        walletItemRepository.deleteById(wi.getId());

        Optional<WalletItem> response = walletItemRepository.findById(wi.getId());

        assertFalse(response.isPresent());

    }
}