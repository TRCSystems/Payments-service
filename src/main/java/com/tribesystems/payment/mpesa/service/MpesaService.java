package com.tribesystems.payment.mpesa.service;

import com.google.gson.Gson;
import com.tribesystems.payment.common.dto.ApiResponse;
import com.tribesystems.payment.mpesa.dto.*;
import com.tribesystems.payment.common.utils.DateTimeUtil;
import com.tribesystems.payment.transaction.mapper.TransactionMapper;
import com.tribesystems.payment.transaction.model.ConfirmedTransaction;
import com.tribesystems.payment.transaction.model.Payment;
import com.tribesystems.payment.transaction.model.RegisterC2BCallbacksModel;
import com.tribesystems.payment.transaction.model.Transaction;
import com.tribesystems.payment.transaction.repository.ConfirmedTransactionRepository;
import com.tribesystems.payment.transaction.repository.PaymentRepository;
import com.tribesystems.payment.transaction.repository.RegisterC2BCallbacksRepository;
import com.tribesystems.payment.transaction.repository.TransactionRepository;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

@Service
public class MpesaService {

    @Value("${mpesa.auth-url}")
    private String mpesaAuthUrl;
    @Value("${mpesa.consumer-secret}")
    private String consumerSecret;
    @Value("${mpesa.consumer-key}")
    private String consumerKey;
    @Value("${mpesa.security-credential}")
    private String securityCredential;
    @Value("${mpesa.account-ref}")
    private String accountRef;

    @Value("${mpesa.initiate-stk-push-url}")
    private String initiateStkPushUrl;
    @Value("${mpesa.query-txn-status}")
    private String queryTxnStatusUrl;

    @Value("${mpesa.passkey}")
    private String passkey;
    @Value("${mpesa.shortcode}")
    private String shortCode;
    @Value("${mpesa.org-shortcode}")
    private String orgShortCode;
    @Value("${mpesa.till-number}")
    private String tillNumber;
    @Value("${mpesa.callback-url}")
    private String callbackUrl;

    @Value("${mpesa.confirm-result-url}")
    private String confirmPaymentResultUrl;
    @Value("${mpesa.confirm-timeout-url}")
    private String confirmPaymentQueueTimeoutUrl;

    @Value("${mpesa.register-c-to-b-callback-url}")
    private String registerC2BCallbackUrlsUrl;
    @Value("${mpesa.simulate-c-to-b-transaction}")
    private String simulateC2BTransactionUrl;

    @Value("${mpesa.c2b-validation-callback}")
    private String c2bValidationCallbackUrl;
    @Value("${mpesa.c2b-confirmation-callback}")
    private String c2bConfirmationCallbackUrl;

    @Value("${mpesa.b-to-c-url}")
    private String b2cUrl;
    @Value("${mpesa.b-to-c-api-username}")
    private String b2cApiUsername;

    @Value("${mpesa.b2c-result-callback}")
    private String b2cResultCallbackUrl;
    @Value("${mpesa.b2c-timeout-callback}")
    private String b2cTimeoutCallbackUrl;

    @Autowired
    private OkHttpClient client;

    @Autowired
    private Gson gson;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RegisterC2BCallbacksRepository registerCallbacksRepository;

    @Autowired
    private ConfirmedTransactionRepository confirmedTransactionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private final Logger logger = LoggerFactory.getLogger(MpesaService.class);

    private String generateUniqueueId(){return UUID.randomUUID().toString();}

    public ApiResponse<AuthenticateAppResponse> authenticateWithMpesa()
    {
        try{
            logger.info("========================================Authenticating client========================================");
            String url = mpesaAuthUrl;

            String authString = consumerKey + ":" + consumerSecret;

            String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes());

            logger.info("=======================================Encoded Url=======================================");
            logger.info(url);
            logger.info("=======================================Auth String=======================================");
            logger.info(encodedAuthString);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization" ,"Basic " + encodedAuthString)
                    .build();

            Response resp = client.newCall(request).execute();
            logger.info("{}", resp);

            if(resp.isSuccessful() && resp.body() != null)
            {
                AuthenticateAppResponse authResp = gson.fromJson(resp.body().string(), AuthenticateAppResponse.class);
                logger.info("{}", authResp);
                return new ApiResponse<>(
                        200,
                        "success",
                        authResp
                );
            }
            else{
                return new ApiResponse<>(
                        500,
                        "failed",
                        new AuthenticateAppResponse(null, null)
                );
            }

        }catch(Exception e)
        {
            logger.error("Error: {}", e.getMessage());
            return new ApiResponse<>(
                    500,
                    "failed",
                    new AuthenticateAppResponse(null, null)
            );
        }
    }

    private String getMpesaPassword(){return Base64.getEncoder().encodeToString(new String(shortCode + passkey + DateTimeUtil.formatedDateTime()).getBytes());}

    public ApiResponse<InitiatePaymentResponse> initiatePayment(InitiatePaymentDto initiatePaymentDto)
    {
        try{
            String timestamp = DateTimeUtil.formatedDateTime();
            String password = getMpesaPassword();
            String testDesc = "Payment";
            String testAccRef = accountRef;
            String tillTransactionType = "CustomerPayBillOnline";

            InitiatePaymentRequest initReq = new InitiatePaymentRequest(
                    password,
                    shortCode,
                    timestamp,
                    "" + initiatePaymentDto.amount(),
                    initiatePaymentDto.phoneNumber(),
                    tillNumber,
                    tillTransactionType,
                    initiatePaymentDto.phoneNumber(),
                    testDesc,
                    testAccRef,
                    callbackUrl
            );
            logger.info("==================================================== Request Body ====================================================");
            logger.info("{}", initReq);

            RequestBody body = RequestBody.create(gson.toJson(initReq), MediaType.get("application/json"));
            logger.info("==================================================== Encoded Request Body ====================================================");
            logger.info("{}", body);

            String token = authenticateWithMpesa().data().access_token();

            if(token == null)
            {
                logger.info("Failed to authenticate with mpesa");
                logger.info("Token: {}", token);
                return new ApiResponse<>(
                        500,
                        "failed",
                        new InitiatePaymentResponse(null, null, -1, null, null)
                );
            }

            Request req = new Request.Builder()
                    .url(initiateStkPushUrl)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            logger.info("===================================initiating payment===================================");
            logger.info("{}", req);

            Response resp = client.newCall(req).execute();

            if(resp.isSuccessful() && resp.body() != null)
            {
                logger.info("=================================== Status code ===================================");
                logger.info("{}", resp.code());
                String bodyStr = resp.body().string();
                logger.info("{}", bodyStr);
                InitiatePaymentResponse initPmtResp = gson.fromJson(bodyStr, InitiatePaymentResponse.class);

                logger.info("=========================================converting response to transaction=========================================");
                Transaction transaction = TransactionMapper.initiatePaymentResponseToTransactionMapper(initPmtResp, initiatePaymentDto, "PENDING");
                logger.info("=========================================after converting response to transaction=========================================");
                logger.info("=========================================saving transaction=========================================");
                transactionRepository.save(transaction);
                logger.info("=========================================successfully saved transaction=========================================");
                return new ApiResponse<>(
                        200,
                        "success",
                        initPmtResp
                );
            }
            else{
                logger.info("=================================== Status code ===================================");
                logger.info("{}", resp.code());
                logger.info("Failed to initiate payment");
                logger.info("{}", resp);
                String bodyStr = resp.body().string();
                logger.info("Response Body: {}", bodyStr);
                return new ApiResponse<>(
                        500,
                        "failed",
                        new InitiatePaymentResponse(null, null, -1, null, null)
                );
            }
        }catch(Exception e)
        {
            logger.error("Failed to initiate stk push: {}", e.getMessage());
            return new ApiResponse<>(
                    500,
                    "failed",
                    new InitiatePaymentResponse(null, null, -1, null, null)
            );
        }
    }

    public ApiResponse<ConfirmPaymentStatusResponse> checkTransactionStatus(CheckTxnStatusDto checkTxnStatusDto)
    {
        try{
            logger.info("initiating payment");
            String token = authenticateWithMpesa().data().access_token();

            String initiator = "TRIBE_SYSTEMS";
            String commandId = "TransactionStatusQuery";
            String txnId = "";
            String idType = "4";
            String remarks = "OK";
            String occasion = "OK";

            CheckTxnStatusRequest reqData = new CheckTxnStatusRequest(
                    initiator,
                    securityCredential,
                    commandId,
                    txnId,
                    checkTxnStatusDto.originatorConversationId(),
                    shortCode,
                    idType,
                    confirmPaymentResultUrl,
                    confirmPaymentQueueTimeoutUrl,
                    remarks,
                    occasion
            );
            logger.info("=======================================Request Body=======================================");
            logger.info("{}", reqData);

            RequestBody body = RequestBody.create(
                    gson.toJson(reqData), MediaType.parse("application/json")
            );

            logger.info("=======================================Request Body=======================================");
            logger.info("{}", body.toString());
            Request request = new Request.Builder()
                    .url(queryTxnStatusUrl)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            logger.info("Payment checked successfully");

            if(response.isSuccessful() && response.body() != null)
            {
                logger.info("{}", response.code());
                logger.info("{}", response.message());
                String bodystr = response.body().string();
                logger.info("{}", bodystr);

                ConfirmPaymentStatusResponse confResp = gson.fromJson(bodystr, ConfirmPaymentStatusResponse.class);
                return new ApiResponse<>(
                        200,
                        "success",
                        confResp
                );
            }
            else{
                logger.info("{}", response.code());
                logger.info(response.message());
                String bodyStr = response.body().string();
                logger.info("Response Body: {}", bodyStr);
                return new ApiResponse<>(
                        500,
                        "failed",
                        new ConfirmPaymentStatusResponse(null, null, -1, null)
                );
            }
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
            return new ApiResponse<>(
                    500,
                    "failed",
                    new ConfirmPaymentStatusResponse(null, null, -1, null)
            );
        }
    }

    public ApiResponse<RegisterC2BUrlsResponse> registerC2BValidationAndConfirmationCallbacks()
    {
        try{
            RegisterC2BUrlsRequest registerReq = new RegisterC2BUrlsRequest(
                    orgShortCode,
                    "Completed",
                    c2bConfirmationCallbackUrl,
                    c2bValidationCallbackUrl
            );
            String token = authenticateWithMpesa().data().access_token();
            logger.info("Sending Request: {}", registerReq);

            RequestBody reqBody = RequestBody.create(
                    gson.toJson(registerReq), MediaType.parse("application/json")
            );

            logger.info("=======================================Request Body=======================================");
            logger.info("{}", reqBody.toString());
            logger.info("Invoking Url: {}", registerC2BCallbackUrlsUrl);
            Request request = new Request.Builder()
                    .url(registerC2BCallbackUrlsUrl)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .post(reqBody)
                    .build();

            logger.info("Request body created successfully");
            Response response = client.newCall(request).execute();
            logger.info("Validation and Confirmation URLs registered successfully");
            logger.info("Received Code: {}", response.code());
            logger.info("Received Message: {}", response.message());

            if(response.isSuccessful() && response.body() != null)
            {
                String bodyStr = response.body().string();
                logger.info("Response Body: {}", bodyStr);
                RegisterC2BUrlsResponse regResp = gson.fromJson(bodyStr, RegisterC2BUrlsResponse.class);
                logger.info("======================== Register Urls Response ========================");
                logger.info("{}", regResp);

                logger.info("creating result model");
                RegisterC2BCallbacksModel model = RegisterC2BCallbacksModel.builder()
                        .registerCallbacksTransactionId(regResp.OriginatorCoversationID())
                        .build();
                logger.info("after creating result model");

                logger.info("saving result");
                registerCallbacksRepository.save(model);
                logger.info("After saving result");

                return new ApiResponse<>(
                        200,
                        "success",
                        regResp
                );
            }
            else{
                logger.info("Failed to register callback urls");
                String bodyStr = response.body().string();
                logger.info("Response Body: {}", bodyStr);
                return new ApiResponse<>(
                        500,
                        "failed",
                        null
                );
            }
        }catch (Exception e)
        {
            logger.error("Failed to register C2B transaction validation and confirmation urls");
            logger.info("{}", e.getMessage());
            return new ApiResponse<>(
                    500,
                    "failed",
                    null
            );
        }
    }

    public ApiResponse<SimulatePaymentForC2BResponse> simulateTransaction(int amount)
    {
        try{

            SimulatePaymentForC2BRequest request = new SimulatePaymentForC2BRequest(
                    600984,
                    "CustomerPayBillOnline",//"CustomerBuyGoodsOnline",
                    amount,
                    new BigInteger("254708374149"),
                    "TestRef"
            );
            String token = authenticateWithMpesa().data().access_token();
            logger.info("Sending Request: {}", request);

            RequestBody reqBody = RequestBody.create(
                    gson.toJson(request), MediaType.parse("application/json")
            );

            logger.info("=======================================Request Body=======================================");
            logger.info("{}", reqBody.toString());
            logger.info("Invoking Url: {}", simulateC2BTransactionUrl);
            Request req = new Request.Builder()
                    .url(simulateC2BTransactionUrl)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .post(reqBody)
                    .build();

            logger.info("Request body created successfully");
            Response response = client.newCall(req).execute();
            logger.info("Simulating payment transaction successful");
            logger.info("Received Code: {}", response.code());
            logger.info("Received Message: {}", response.message());

            if(response.isSuccessful() && response.body() != null)
            {
                String bodyStr = response.body().string();
                logger.info("Response Body: {}", bodyStr);
                SimulatePaymentForC2BResponse simResp = gson.fromJson(bodyStr, SimulatePaymentForC2BResponse.class);
                logger.info("======================== Simulate Transaction Response ========================");
                logger.info("{}", simResp);

                return new ApiResponse<>(
                        200,
                        "success",
                        simResp
                );
            }
            else{
                logger.info("Failed to simulate transaction");
                String bodyStr = response.body().string();
                logger.info("Response Body: {}", bodyStr);
                return new ApiResponse<>(
                        500,
                        "failed",
                        null
                );
            }
        }catch (Exception e)
        {
            logger.error("Failed to simulate C2B transaction");
            logger.info("{}", e.getMessage());
            return new ApiResponse<>(
                    500,
                    "failed",
                    null
            );
        }
    }

    public ValidateC2BPaymentResponse transactionValidationCallback(TransactionCallbackRequest request)
    {
        try{
            logger.info("========================================== Received a transaction for validation ==========================================");
            logger.info("{}", request);
            logger.info("========================================== Saving the transaction for validation ==========================================");
            confirmedTransactionRepository.save(TransactionMapper.transactionCallbackRequestToConfirmedTransactionMapper(request, "PENDING"));
            logger.info("========================================== After saving the transaction for validation ==========================================");
            logger.info("========================================== Completed validation of transaction ==========================================");
        }catch(Exception e)
        {
            logger.error("Failed to validate transaction!");
            logger.error("Reason: {}", e.getMessage());
        }
        return new ValidateC2BPaymentResponse("0","Accepted");
    }

    public void transactionConfirmationCallback(TransactionCallbackRequest request)
    {
        try{
            logger.info("========================================== Received a transaction for confirmation ==========================================");
            logger.info("{}",request);
            logger.info("============================ Checking if transaction exists =============================");
            Optional<ConfirmedTransaction> result = confirmedTransactionRepository.findConfirmedTransactionByMpesaTransactionId(request.TransID());

            if(result.isEmpty())
            {
                logger.info("====================================== Transaction does not exist. Creating a new record for the transaction ======================================");
                confirmedTransactionRepository.save(TransactionMapper.transactionCallbackRequestToConfirmedTransactionMapper(request, "COMPLETED"));
                logger.info("====================================== After Creating a new record for the transaction ======================================");
            }
            else{
                logger.info("====================================== Transaction does exist. Updating existing record of the transaction ======================================");
                ConfirmedTransaction trans = result.get();
                trans.setMpesaTransactionStatus("COMPLETED");
                confirmedTransactionRepository.save(trans);
                logger.info("====================================== After Updating existing record of the transaction ======================================");
            }
            logger.info("========================================== Completed confirmation of transaction ==========================================");
        }catch(Exception e)
        {
            logger.error("Failed to confirm transaction!");
            logger.error("Reason: {}", e.getMessage());
        }
    }

    public ApiResponse<List<Transaction>> getAllTransactions()
    {
        try{
            return new ApiResponse<>(
                    200,
                    "success",
                    transactionRepository.findAll()
            );
        }
        catch (Exception e)
        {
            logger.error("Failed to fetch all stk push transactions");
            logger.error("reason: {}", e.getMessage());
            return new ApiResponse<>(
                    500,
                    "failed",
                    new ArrayList<>()
            );
        }
    }

    public ApiResponse<List<ConfirmedTransaction>> getAllC2BTransactions()
    {
        try{
            return new ApiResponse<>(
                    200,
                    "success",
                    confirmedTransactionRepository.findAll()
            );
        }
        catch (Exception e)
        {
            logger.error("Failed to fetch all stk push transactions");
            logger.error("reason: {}", e.getMessage());
            return new ApiResponse<>(
                    500,
                    "failed",
                    new ArrayList<>()
            );
        }
    }

    public ApiResponse<B2CResponse> initiateB2Cpayment(InitiatePaymentDto dto)
    {
        try{

            B2CRequest req = new B2CRequest(
                    generateUniqueueId(),
                    b2cApiUsername,
                    securityCredential,
                    "BusinessPayment", //"SalaryPayment",
                    "" + dto.amount(),
                    orgShortCode,
                    dto.phoneNumber(),//254705912645
                    "Payment Due",
                    b2cTimeoutCallbackUrl,
                    b2cResultCallbackUrl,
                    "Occassion For Payment"
            );
            String token = authenticateWithMpesa().data().access_token();
            logger.info("Sending Request: {}", req);

            RequestBody reqBody = RequestBody.create(
                    gson.toJson(req), MediaType.parse("application/json")
            );

            logger.info("=======================================Request Body=======================================");
            logger.info("{}", reqBody.toString());
            logger.info("Invoking Url: {}", b2cUrl);
            Request request = new Request.Builder()
                    .url(b2cUrl)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .post(reqBody)
                    .build();

            logger.info("Request body created successfully");
            Response response = client.newCall(request).execute();
            logger.info("Initiating B2C Payment Successful");
            logger.info("Received Code: {}", response.code());
            logger.info("Received Message: {}", response.message());

            if(response.isSuccessful() && response.body() != null)
            {
                String bodyStr = response.body().string();
                logger.info("Response Body: {}", bodyStr);
                B2CResponse simResp = gson.fromJson(bodyStr, B2CResponse.class);
                logger.info("======================== Initiate B2C Payment Successful ========================");
                logger.info("{}", simResp);
                logger.info("======================================== Creating new payment object ========================================");
                Payment pmt = TransactionMapper.b2cRequestToPaymentMapper(req, "PENDING");
                logger.info("======================================== After Creating new payment object ========================================");

                logger.info("======================================== saving the new payment ========================================");
                paymentRepository.save(pmt);
                logger.info("======================================== after saving the new payment ========================================");

                return new ApiResponse<>(
                        200,
                        "success",
                        simResp
                );
            }
            else{
                logger.info("Failed to initiate B2C Payment");
                String bodyStr = response.body().string();
                logger.info("Response Body: {}", bodyStr);
                return new ApiResponse<>(
                        500,
                        "failed",
                        null
                );
            }
        }catch (Exception e)
        {
            logger.error("Failed to initiate B2C Payment");
            logger.info("{}", e.getMessage());
            return new ApiResponse<>(
                    500,
                    "failed",
                    null
            );
        }
    }

    public ApiResponse<List<Payment>> getAllB2CPayments()
    {
        try{
            return new ApiResponse<>(
                    200,
                    "success",
                    paymentRepository.findAll()
            );
        }
        catch (Exception e)
        {
            logger.error("Failed to fetch all B2C Payments");
            logger.error("reason: {}", e.getMessage());
            return new ApiResponse<>(
                    500,
                    "failed",
                    new ArrayList<>()
            );
        }
    }

    public void processB2CCallbackResult(B2CCallbackResult result)
    {
        try{
            String id = result.Result().OriginatorConversationID();
           Optional<Payment> pmt = paymentRepository.findPaymentByPaymentOriginatorConversationID(id);

           if(pmt.isEmpty())
           {
               logger.info("No payment with specified id exists");
               logger.info("Payment: {}", result);
           }
           else{
               logger.info("Payment with specified ID has been found");
               logger.info("Payment: {}", pmt);
               Payment payment = pmt.get();
               payment.setPaymentStatus("COMPLETED");
               paymentRepository.save(payment);
           }
        }
        catch (Exception e)
        {
            logger.error("Failed to process B2C Callback result");
            logger.error("reason: {}", e.getMessage());
        }
    }

    public void processB2CCallbackTimeout(B2CCallbackResult result)
    {
        try{
            logger.error("Payment processing timed out!");
            logger.error("Result: {}", result);
        }
        catch (Exception e)
        {
            logger.error("Failed to process B2C Callback timeout");
            logger.error("reason: {}", e.getMessage());
        }
    }
}
