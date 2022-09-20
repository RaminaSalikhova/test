package com.devexperts.dxcore;

import com.devexperts.dxcore.api.localization.impl.LocalizableMessages;
import com.devexperts.dxcore.entities.AccountContent;
import com.devexperts.dxcore.entities.Order;
import com.devexperts.dxcore.entities.OrderLeg;
import com.devexperts.dxcore.entities.OrderLegBuilder;
import com.devexperts.dxcore.entities.OrderSide;
import com.devexperts.dxcore.entities.instruments.InstrumentBuilder;
import com.devexperts.dxcore.entities.instruments.InstrumentType;
import com.devexperts.dxcore.entities.instruments.OptionSide;
import com.devexperts.dxcore.entities.instruments.TradableInstrument;
import com.devexperts.dxcore.tdw.TdwTradingRestrictionAssociatedDataDomain;
import com.devexperts.dxcore.tdw.orderhistory.TdwAdminOrderType;
import com.devexperts.dxcore.tdw.tradingrestrictions.ActionType;
import com.devexperts.dxcore.tdw.tradingrestrictions.BlockInstrumentType;
import com.devexperts.dxcore.tdw.tradingrestrictions.OptionType;
import com.devexperts.dxcore.tdw.tradingrestrictions.PositionEffect;
import com.devexperts.dxcore.tdw.tradingrestrictions.Side;
import com.devexperts.dxcore.tdw.tradingrestrictions.TdwTradingRestriction;
import com.devexperts.dxcore.tdw.tradingrestrictions.TdwTradingRestrictionFactory;
import com.devexperts.dxcore.tdw.tradingrestrictions.TdwTradingRestrictions;
import com.devexperts.dxcore.testbase.WiseUnitDxCoreTestBase;
import com.devexperts.dxcore.trading.operations.positions.StrategyType;
import com.devexperts.dxcore.utils.OrderUtils;
import com.devexperts.dxfx.common.validation.ValidationContext;
import com.devexperts.dxfx.common.validation.entities.ProcessingStage;
import com.devexperts.dxfx.common.validation.rules.RuleFailReason;
import com.devexperts.tests.categories.UnitTest;
import org.junit.Before;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.devexperts.dxcore.tdw.instruments.TdwInstrumentsFieldsDomain.IBM_SEC_NUMBER;
import static com.devexperts.dxcore.utils.OrderUtils.ACCOUNT_KEY_1;
import static com.devexperts.dxfx.common.validation.rules.RuleFailReason.newRuleFailReason;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
public class TradingRestrictionsValidationRuleTest extends WiseUnitDxCoreTestBase {
    private final OurValidationRule validator = new OurValidationRule();

    private final ProcessingStage processingStage = ProcessingStage.ORDER_ACTIVATION;

    private final AccountContent account = mock(AccountContent.class);

    private final ValidationContext context = mock(ValidationContext.class);

    private static final String clientReasonSymbolBlocking="This security is blocked from trading. Please contact Client Services for details. ";

    private static final String clientReasonCTO="This security and all its derivatives are blocked from trading. Code: 316";

    private static final String clientReasonStockWatch="";

    private static final String clientReasonTender="Due to a Tender Offer, some or all of these shares are restricted from selling at this time. ";

    private static final String clientReasonNonResident="This security is unavailable to be traded in your area. Please contact Client Services for details. ";

    private static final String VALID_IBM_SEC_NUMB = "123456";

    private static final String SYMBOL_BLOCK_IBM_SEC_NUMB = "SymbolBlock";

    private static final String CTO_IBM_SEC_NUMB = "CTO";

    private static final String STOCK_WATCH_IBM_SEC_NUMB = "StockWatch";

    private static final String NON_RESIDENT_IBM_SEC_NUMB = "NonResident";

    private static final String TENDER_IBM_SEC_NUMB = "Tender";
    private static final String ACCOUNT_ID = "accountId";

    private final TdwTradingRestrictions tdwTradingRestrictions;

    public TradingRestrictionsValidationRuleTest() {
        tdwTradingRestrictions=new TdwTradingRestrictions(Stream.of(createCTORestriction(), createTenderRestriction(), createNonResidentRestriction(),
                        createStockWatchRestriction(), createSymbolBlockRestriction())
                .collect(Collectors.toList()));
    }

    @Before
    public void setUp() {
        when(context.getSystemAssociatedDataOrNull(TdwTradingRestrictionAssociatedDataDomain.RESTRICTIONS)).thenReturn(tdwTradingRestrictions);
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void validateOrder(Order order, LocalizableMessages expected) {
        RuleFailReason validationResult = validator.validateOrder(order, processingStage, account, context);
        assertEquals(expected, validationResult.getRejectMessage().getMessageDescriptor());
    }

    private static Stream<Order> provideTestData() {
        return Stream.of(
                Arguments.of(createOrder(VALID_IBM_SEC_NUMB), null),
                Arguments.of(createOrder(SYMBOL_BLOCK_IBM_SEC_NUMB), newRuleFailReason(LocalizableMessages.ORDER_RESTRICTIONS_REJECT.withParameters(clientReasonSymbolBlocking))),
                Arguments.of(createOrder(CTO_IBM_SEC_NUMB), newRuleFailReason(LocalizableMessages.ORDER_RESTRICTIONS_REJECT.withParameters(clientReasonCTO))),
                Arguments.of(createOrder(STOCK_WATCH_IBM_SEC_NUMB), newRuleFailReason(LocalizableMessages.ORDER_RESTRICTIONS_REJECT.withParameters(clientReasonStockWatch))),
                Arguments.of(createOrder(NON_RESIDENT_IBM_SEC_NUMB), newRuleFailReason(LocalizableMessages.ORDER_RESTRICTIONS_REJECT.withParameters(clientReasonNonResident))),
                Arguments.of(createOrder(TENDER_IBM_SEC_NUMB), newRuleFailReason(LocalizableMessages.ORDER_RESTRICTIONS_REJECT.withParameters(clientReasonTender)))
        );
    }

    private static Order createOrder(String ibmSecurityNumber ) {
        TradableInstrument tradableInstrument = createTradeInstrument(ibmSecurityNumber);

        OrderLeg leg1 = createLeg(tradableInstrument);
        OrderLeg leg2 = createLeg(tradableInstrument);

        return OrderUtils.createOrder(ACCOUNT_KEY_1, 19., OrderSide.SELL, "", com.devexperts.dxcore.entities.PositionEffect.OPENING)
                .setLegs(
                        leg1,
                        leg2
                )
                .toOrder();
    }

    private static OrderLeg createLeg(TradableInstrument instrument) {
        return new OrderLegBuilder()
                .setInstrument(instrument)
                .setPositionCode("IBM_STOCK")
                .setRatioQuantity(10)
                .toOrderLeg();
    }

    private static TradableInstrument createTradeInstrument(String ibmSecurityNumber) {
        return new InstrumentBuilder()
                .setCountry("US")
                .setOptionSide(OptionSide.CALL)
                .setType(InstrumentType.BOND)
                .addAdditionalField(IBM_SEC_NUMBER, ibmSecurityNumber)
                .toInstrument().asTradableInstrument();
    }

    private static TdwTradingRestriction createSymbolBlockRestriction() {
        return TdwTradingRestrictionFactory.createSymbolBlockTradingRestriction(null,
                Collections.singletonList("IBM"),
                Collections.singletonList(SYMBOL_BLOCK_IBM_SEC_NUMB),
                BlockInstrumentType.ALL,
                Collections.singletonList(StrategyType.STOCK),
                OptionType.CALL_AND_PUT,
                Collections.singletonList(TdwAdminOrderType.MARKET),
                Side.BOTH,
                PositionEffect.CLOSING,
                Collections.singletonList("accountId"),
                ActionType.REJECTION,
                clientReasonSymbolBlocking,
                "reasonAdmin",
                100,
                200);
    }

    private static TdwTradingRestriction createStockWatchRestriction() {
        return TdwTradingRestrictionFactory.createStockWatchTradingRestriction(null,
                Collections.singletonList("IBM"),
                Collections.singletonList(STOCK_WATCH_IBM_SEC_NUMB),
                Side.BOTH,
                clientReasonStockWatch,
                "reasonAdmin",
                100,
                200);
    }

    private static TdwTradingRestriction createTenderRestriction() {
        return TdwTradingRestrictionFactory.createTenderTradingRestriction(null,
                Collections.singletonList("IBM"),
                Collections.singletonList(TENDER_IBM_SEC_NUMB),
                Collections.singletonList(ACCOUNT_ID),
                100,
                clientReasonTender,
                "reasonAdmin",
                100,
                200);
    }

    private static TdwTradingRestriction createCTORestriction() {
        return TdwTradingRestrictionFactory.createCTOTradingRestriction(null,
                Collections.singletonList("IBM"),
                Collections.singletonList(CTO_IBM_SEC_NUMB),
                Side.BOTH,
                clientReasonCTO,
                "reasonAdmin",
                100,
                200);
    }

    private static TdwTradingRestriction createNonResidentRestriction() {
        return TdwTradingRestrictionFactory.createNonResidentTradingRestriction(null,
                Collections.singletonList("IBM"),
                Collections.singletonList(NON_RESIDENT_IBM_SEC_NUMB),
                Side.BOTH,
                Collections.singletonList("US"),
                clientReasonNonResident,
                "reasonAdmin",
                100,
                200);
    }
}
