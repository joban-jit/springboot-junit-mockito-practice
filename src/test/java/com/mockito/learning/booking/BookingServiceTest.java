package com.mockito.learning.booking;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.function.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private PaymentService paymentServiceMock;

    @Mock
    private RoomService roomServiceMock;

    @Mock
    private BookingDAO bookingDAOMock;

    @Mock
    private MailSender mailSenderMock;

    @Captor
    private ArgumentCaptor<Double> doubleCaptor;


    // Replaced below with annotations
//    @BeforeEach
//    void setup() {
//        this.paymentServiceMock = mock(PaymentService.class);
//        this.roomServiceMock = mock(RoomService.class);
//        this.bookingDAOMock = mock(BookingDAO.class);
//        this.mailSenderMock = mock(MailSender.class);
//        this.bookingService = new BookingService(
//                paymentServiceMock,
//                roomServiceMock,
//                bookingDAOMock,
//                mailSenderMock
//        );
//
//        this.doubleCaptor = ArgumentCaptor.forClass(Double.class);
//    }

    //basic

    @Test
    void should_CalculateCorrectPrice_When_CorrectInput() {

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, false
        );
        double expectedPrice = 5 * 2 * 50.0;

        double actualPrice = bookingService.calculatePrice(bookingRequest);
        assertEquals(expectedPrice, actualPrice);
    }

    // default return values
    @Test
    void test_getAvaiablePlaceCount_When_CorrectInput_Retuns_defaultValues() {
        int actualCount = bookingService.getAvailablePlaceCount();
        assertEquals(0, actualCount);
    }

    // customer return values
    @Test
    void test_getAvaiablePlaceCount_When_OneRoomAvailable() {
        when(roomServiceMock.getAvailableRooms()).thenReturn(Collections.singletonList(new Room("1", 2)));
        int actualCount = bookingService.getAvailablePlaceCount();
        assertEquals(2, actualCount);
    }

    @Test
    void test_getAvaiablePlaceCount_When_MultipleRoomsAvailable() {
        Room room1 = new Room("1", 2);
        Room room2 = new Room("2", 3);
        when(roomServiceMock.getAvailableRooms()).thenReturn(List.of(room1, room2));
        System.out.println(roomServiceMock.getRoomCount());
        int actualCount = bookingService.getAvailablePlaceCount();
        assertEquals(5, actualCount);
    }

    // multiple returns
    @Test
    void should_countAvailablePlaceCount_When_CalledMultipleTimes() {
        when(roomServiceMock.getAvailableRooms())
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.singletonList(new Room("1", 2)));
        int actualFirst = bookingService.getAvailablePlaceCount();

        int actualSecond = bookingService.getAvailablePlaceCount();
        assertAll(
                () -> assertEquals(0, actualFirst),
                () -> assertEquals(2, actualSecond)
        );
    }


    // argument matchers
    @Test
    void shouldThrowException_CalculateCorrectPrice_When_NoRoomAvailable() {

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, true
        );

        when(roomServiceMock.findAvailableRoomId(Mockito.any(BookingRequest.class)))
                .thenThrow(BusinessException.class);
        Executable executable = () -> bookingService.makeBooking(bookingRequest);
        assertThrows(BusinessException.class, executable);
    }


    @Test
    void shouldNotCompleteBooking_WhenPriceTooHigh() {

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, true
        );
        when(paymentServiceMock.pay(any(), anyDouble())).thenThrow(BusinessException.class);

        Executable executable = () -> bookingService.makeBooking(bookingRequest);
        assertThrows(BusinessException.class, executable);
    }

    // throw exception

    @Test
    void givenForSpecificPriceAndAnyBookingRequest_shouldNotCompleteBooking_WhenPriceTooHigh_byUsingEqMethod() {

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, true
        );
        when(paymentServiceMock.pay(any(), eq(500.0))).thenThrow(BusinessException.class);

        Executable executable = () -> bookingService.makeBooking(bookingRequest);
        assertThrows(BusinessException.class, executable);
    }

    // verify and verifyNoMoreInteractions

    @Test
    void should_InvokePayment_When_Prepaid() {

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, true
        );
        bookingService.makeBooking(bookingRequest);

        verify(paymentServiceMock, times(1)).pay(bookingRequest, 500.0);
        verifyNoMoreInteractions(paymentServiceMock);
    }


    @Test
    void shouldNot_InvokePayment_When_NotPrepaid() {

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, false
        );

        bookingService.makeBooking(bookingRequest);

        verify(paymentServiceMock, never()).pay(any(), anyDouble());

    }

    // spies - aka partials mocks
    // NOTE: mock = dummy object with no real logic
    // while spy = real object with real logic that we can modify

    @Test
    void should_MakingBooking_When_Input() {

        // changed BookingDAO to mock
        this.bookingDAOMock = spy(BookingDAO.class);
        this.bookingService = new BookingService(
                paymentServiceMock,
                roomServiceMock,
                bookingDAOMock,
                mailSenderMock
        );

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, true
        );
        String bookingId = bookingService.makeBooking(bookingRequest);
        verify(bookingDAOMock).save(bookingRequest);
        System.out.println("bookingid=" + bookingId);

    }


    @Test
    void should_CancelBooking_When_InputOk() {

        // changed BookingDAO to mock
        this.bookingDAOMock = spy(BookingDAO.class);
        this.bookingService = new BookingService(
                paymentServiceMock,
                roomServiceMock,
                bookingDAOMock,
                mailSenderMock
        );

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, true
        );

        bookingRequest.setRoomId("1.3");
        String bookingId = "1";

        // for spy - this way we uses 'when' statement
        doReturn(bookingRequest).when(bookingDAOMock).get(bookingId);

        bookingService.cancelBooking(bookingId);
//        verify(bookingDAOMock).save(bookingRequest);
//        System.out.println("bookingid=" + bookingId);

    }

    // mock void methods

    @Test
    void shouldThrowException_When_MailNotCorrect() {

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, true
        );

        // we can't use when statment with void method like below
//        when(this.mailSenderMock.sendBookingConfirmation(any())).thenThrow(BusinessException.class)
        doThrow(new BusinessException()).when(mailSenderMock).sendBookingConfirmation(any());
        Executable executable = () -> bookingService.makeBooking(bookingRequest);
        assertThrows(BusinessException.class, executable);
    }


    @Test
    void shouldNotThrowException_When_MailNotCorrect() {

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, true
        );

        // we can't use when statment with void method like below
//        when(this.mailSenderMock.sendBookingConfirmation(any())).thenThrow(BusinessException.class)
        doNothing().when(mailSenderMock).sendBookingConfirmation(any()); // doNothing is default behaviour of void method
        // so if you want you can comment this as well
        bookingService.makeBooking(bookingRequest);

    }

    // Argument Captors - to find what arguments passed to a method

    @Test
    void should_PayCorrectPrice_When_InputOk() {

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, true
        );
        bookingService.makeBooking(bookingRequest);

        // in below line we are capturing the argument
        verify(paymentServiceMock, times(1)).pay(eq(bookingRequest), doubleCaptor.capture());
        // now we are saving it some field
        double capturedArgument = doubleCaptor.getValue();
        System.out.println(capturedArgument);
        // using that captured argument
        assertEquals(500.0, capturedArgument);

    }


    @Test
    void should_PayCorrectPrice_When_MultipleCalls() {

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, true
        );
        BookingRequest bookingRequest2 = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 22), 2, true
        );
        List<Double> expectedValues = List.of(500.0, 100.0);
        bookingService.makeBooking(bookingRequest);
        bookingService.makeBooking(bookingRequest2);

        verify(paymentServiceMock, times(2)).pay(any(BookingRequest.class), doubleCaptor.capture());
        List<Double> capturedArgumentList = doubleCaptor.getAllValues();
        assertEquals(expectedValues, capturedArgumentList);

    }

    // Mockito BDD

    @Test
    void testWithBDD_getAvaiablePlaceCount_When_OneRoomAvailable() {
        given(roomServiceMock.getAvailableRooms()).willReturn(Collections.singletonList(new Room("1", 2)));
        int actualCount = bookingService.getAvailablePlaceCount();
        assertEquals(2, actualCount);
    }

    @Test
    void testWithBDD_should_InvokePayment_When_Prepaid() {

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, true
        );
        bookingService.makeBooking(bookingRequest);

        then(paymentServiceMock).should(times(1)).pay(bookingRequest, 500.0);
        verifyNoMoreInteractions(paymentServiceMock);
    }

    // Strict Stubbing
    @Test
    void testWithStrictStubbing_should_InvokePayment_When_Prepaid() {

        BookingRequest bookingRequest = new BookingRequest(
                "1", LocalDate.of(2023, 04, 21),
                LocalDate.of(2023, 04, 26), 2, false
        );
        // so here below mock is not needed as this scenario it won't call this method
        // so it will fail with "UnnecessaryStubbingException" because by default strict stubbing is enabled
//        when(paymentServiceMock.pay(any(), anyDouble())).thenReturn("1");
        // but if you still want to keep this stubbing you can use lenient() - but not recommended
        lenient().when(paymentServiceMock.pay(any(), anyDouble())).thenReturn("1");

        bookingService.makeBooking(bookingRequest);
    }

    // Mockito with static methods (used mockito-inline dependency )
    @Test
    void testStaticMethod_should_CalculateCorrectPrice() {

        try (MockedStatic<CurrencyConverter> mockedConverter
                     = mockStatic(CurrencyConverter.class)) {

            BookingRequest bookingRequest
                    = new BookingRequest(
                    "1", LocalDate.of(2023, 04, 21),
                    LocalDate.of(2023, 04, 26), 2, false
            );
            double expected = 400.0;
            mockedConverter.when(() -> CurrencyConverter.toEuro(anyDouble())).thenReturn(400.0);
            double actual = bookingService.calculatePriceEuro(bookingRequest);
            assertEquals(expected, actual);
        }


    }

    // making above test better using 'answer' method
    @Test
    void testStaticMethodWithAnswer_should_CalculateCorrectPrice() {

        try (MockedStatic<CurrencyConverter> mockedConverter
                     = mockStatic(CurrencyConverter.class)) {

            BookingRequest bookingRequest
                    = new BookingRequest(
                    "1", LocalDate.of(2023, 04, 21),
                    LocalDate.of(2023, 04, 26), 2, false
            );
            double expected = 500.0 * 0.8;// you don't have to use this *0.8
            mockedConverter.when(() -> CurrencyConverter.toEuro(anyDouble()))
                    .thenAnswer(inv -> (double) inv.getArgument(0) * 0.8);// you don't have to use this *0.8
            double actual = bookingService.calculatePriceEuro(bookingRequest);
            assertEquals(expected, actual);
        }
    }

    // final - you can test it just like any other method, but you need to use mockito-inline dependency
    @Test
    void testFinalMethods_getAvaiablePlaceCount_When_OneRoomAvailable() {
        when(roomServiceMock.getAvailableRooms()).thenReturn(Collections.singletonList(new Room("1", 2)));
        int actualCount = bookingService.getAvailablePlaceCount();
        assertEquals(2, actualCount);
    }

}