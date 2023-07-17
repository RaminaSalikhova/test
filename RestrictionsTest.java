import javax.swing.text.html.Option;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Category(UnitTest.class)
class RestrictionsTest extends WiseUnitDxCoreTestBase {
    //edited
    private final OurValidationRule validator = new OurValidationRule();

    private final ProcessingStage processingStage = ProcessingStage.ORDER_ACTIVATION;

    private final AccountContent account = mock(AccountContent.class);

    private final ValidationContext context = mock(ValidationContext.class);

    private static final String IBM_SEC_NUMB = "123456";
    private static final String ACCOUNT_ID = "accountId";

    private final Collections<TdwTradingRestrictionImpl> restrictions;

    @Before
    public void setUp() {
        restrictions =Stream.of(createCTORestriction(), createTenderRestriction(), createNonResidentRestriction(), createStockWatchTradingRestriction(), createSymbolBlockTradingRestriction(ActionType.REJECTION, 300))
                .collect(Collectors.toList());
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void testAssert(Order order, LocalizableMessages expected) {
        RuleFailReason validationResult = validator.validateOrder(order, processingStage, account, context);
        assertEquals(expected, validationResult.getRejectMessage().getMessageDescriptor());
    }

    private static Stream<Order> provideTestData() {
        return Stream.of(
                Arguments.of(createOrder(), null),
                Arguments.of(createOrder(), LocalizableMessages.VALIDATION_FAILED_ORDER_LEGS_HAVE_SAME_INSTRUMENT),
                Arguments.of(createOrder(), LocalizableMessages.VALIDATION_FAILED_ORDER_LEGS_HAVE_SAME_INSTRUMENT),
                Arguments.of(createOrder(), LocalizableMessages.VALIDATION_FAILED_ORDER_LEGS_HAVE_SAME_INSTRUMENT),
                Arguments.of(createOrder(), LocalizableMessages.VALIDATION_FAILED_ORDER_LEGS_HAVE_SAME_INSTRUMENT),
                Arguments.of(createOrder(), LocalizableMessages.VALIDATION_FAILED_ORDER_LEGS_HAVE_SAME_INSTRUMENT)
        );
    }

    private static Order createOrder(Option option, String symbolFor1Leg, double quantityFor1Leg,String symbolFor2Leg, double quantityFor2Leg, double orderQuantity,double limitPrice){
        Option option = createOption(option);

        OrderLegBuilder[] legs = {sleg(symbolFor1Leg, quantityFor1Leg), sleg(symbolFor2Leg, quantityFor2Leg)};

        return createLimitSpreadOrder(ACCOUNT_KEY_1,  orderQuantity, limitPrice, legs);
    }

    private static Order createOrder(String optionSymbol, String positionCode, String symbol, double quantity){
        Option option = createOption(optionSymbol, AAPL_STOCK);

        OrderLeg leg1 = createLeg(option, positionCode);
        OrderLeg leg2 = createLeg(option, positionCode);

        return Order order = createOrder(ACCOUNT_KEY_1, quantity, symbol, PositionEffect.OPENING)
                .setLegs(
                        leg1,
                        leg2
                )
                .toOrder();
    }

    private static OrderLeg createLeg(Option option, String positionCode){
        return new OrderLegBuilder()
                .setInstrument(option)
                .setPositionCode(positionCode)
                .setPositionEffect(PositionEffect.OPENING)
                .setRatioQuantity(10)
                .toOrderLeg();
    }

    private static TdwTradingRestriction createSymbolBlockTradingRestriction(ActionType actionType, long creationDateTime) {
        return new TdwTradingRestrictionImpl(null,
                TdwTradingRestrictionType.SYMBOL_BLOCK,
                2,
                Collections.singletonList("IBM"),
                Collections.singletonList(IBM_SEC_NUMB),
                BlockInstrumentType.ALL,
                Collections.singletonList(StrategyType.STOCK),
                OptionType.CALL_AND_PUT,
                Collections.singletonList(TdwAdminOrderType.MARKET),
                Side.SELL,
                PositionEffect.CLOSING,
                Collections.singletonList("accountId"),
                null,
                null,
                actionType,
                "reasonClient",
                "reasonAdmin",
                100,
                200,
                null,
                null,
                creationDateTime,
                0);
    }

    private static TdwTradingRestriction createStockWatchTradingRestriction() {
        return TdwTradingRestrictionFactory.createStockWatchTradingRestriction(null,
                Collections.singletonList("IBM"),
                Collections.singletonList(IBM_SEC_NUMB),
                Side.BOTH,
                "reasonClient",
                "reasonAdmin",
                100,
                200);
    }

    private static TdwTradingRestriction createTenderRestriction() {
        return TdwTradingRestrictionFactory.createTenderTradingRestriction(null,
                Collections.singletonList("IBM"),
                Collections.singletonList(IBM_SEC_NUMB),
                Collections.singletonList(ACCOUNT_ID),
                100,
                "reasonClient",
                "reasonAdmin",
                100,
                200);
    }

    private static TdwTradingRestriction createCTORestriction() {
        return TdwTradingRestrictionFactory.createCTOTradingRestriction(null,
                Collections.singletonList("IBM"),
                Collections.singletonList(IBM_SEC_NUMB),
                Side.BOTH,
                "reasonClient",
                "reasonAdmin",
                100,
                200);
    }

    private static TdwTradingRestriction createNonResidentRestriction() {
        return TdwTradingRestrictionFactory.createNonResidentTradingRestriction(null,
                Collections.singletonList("IBM"),
                Collections.singletonList(IBM_SEC_NUMB),
                Side.BOTH,
                Collections.singletonList("US"),
                "reasonClient",
                "reasonAdmin",
                100,
                200);
    }
}
