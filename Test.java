import java.time.Clock;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@Category(UnitTest.class)
class Test extends WiseUnitDxCoreTestBase {

    private final OurValidationRule validator = new OurValidationRule();

    private final ProcessingStage processingStage = ProcessingStage.ORDER_ACTIVATION;

    private final AccountContent account = mock(AccountContent.class);

    private final ValidationContext context = mock(ValidationContext.class);

    public static final String IBM_OPTION = ".IBM7140419P430";

    public static final String AAPL_OPTION = ".AAPL7140419P611";

    private final Order order;

    private OrderLeg leg1;

    private OrderLeg leg2;

    private OrderLeg leg3;

    private OrderLeg leg4;

    @Before
    public void setUp() {
        Option optionOnAAPL1 = createOption(AAPL_OPTION, AAPL_STOCK);
        Option optionOnIBM = createOption(IBM_OPTION, IBM_STOCK);
        OrderLeg leg1 = new OrderLegBuilder().
                setInstrument(optionOnAAPL1)
                .setPositionCode("STOCK1")
                .setPositionEffect(OPENING)
                .setRatioQuantity(10)
                .toOrderLeg();
        OrderLeg leg2 = new OrderLegBuilder()
                .setInstrument(optionOnIBM)
                .setPositionCode("STOCK2")
                .setPositionEffect(OPENING)
                .setRatioQuantity(2.5)
                .toOrderLeg();
        OrderLeg leg3 = new OrderLegBuilder().
                setInstrument(optionOnAAPL1)
                .setPositionCode("STOCK1")
                .setPositionEffect(OPENING)
                .setRatioQuantity(10)
                .toOrderLeg();
        OrderLeg leg4 = new OrderLegBuilder()
                .setInstrument(optionOnIBM)
                .setPositionCode("STOCK2")
                .setPositionEffect(OPENING)
                .setRatioQuantity(2.5)
                .toOrderLeg();
    }

    @Test
    public void testAssertNull() {
        Order order = createOrder(ACCOUNT_KEY_1, 19., "NOT_MATTER", PositionEffect.OPENING)
                .setLegs(
                        leg1,
                        leg2
                )
                .toOrder();

        RuleFailReason validationResult = validator.validateOrder(order, processingStage, account, context);

        assertNull(validationResult);
    }

    @Test
    public void testAssertNotNull() {
        Order order = createOrder(ACCOUNT_KEY_1, 19., "NOT_MATTER", PositionEffect.OPENING)
                .setLegs(
                        leg3,
                        leg4
                )
                .toOrder();

        RuleFailReason validationResult = validator.validateOrder(order, processingStage, account, context);

        assertNotNull(validationResult);
        assertEquals(LocalizableMessages.VALIDATION_FAILED_ORDER_LEGS_HAVE_SAME_INSTRUMENT, validationResult.getRejectMessage().getMessageDescriptor());
    }
}