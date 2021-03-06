package org.hyperledger.indy.sdk.ledger;

import org.hyperledger.indy.sdk.IndyIntegrationTestWithPoolAndSingleWallet;
import org.hyperledger.indy.sdk.InvalidStructureException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.json.JSONObject;
import org.junit.*;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AttribRequestsTest extends IndyIntegrationTestWithPoolAndSingleWallet {

	private String endpoint = "{\"endpoint\":{\"ha\":\"127.0.0.1:5555\"}}";

	@Test
	public void testBuildAttribRequestWorksForRawData() throws Exception {
		String expectedResult = String.format("\"identifier\":\"%s\"," +
				"\"operation\":{" +
				"\"type\":\"100\"," +
				"\"dest\":\"%s\"," +
				"\"raw\":\"%s\"" +
				"}", DID_TRUSTEE, DID_TRUSTEE, endpoint);

		String attribRequest = Ledger.buildAttribRequest(DID_TRUSTEE, DID_TRUSTEE, null, endpoint, null).get();

		assertTrue(attribRequest.replace("\\", "").contains(expectedResult));
	}

	@Test
	public void testBuildAttribRequestWorksForMissedAttribute() throws Exception {
		thrown.expect(ExecutionException.class);
		thrown.expectCause(isA(InvalidStructureException.class));

		Ledger.buildAttribRequest(DID_TRUSTEE, DID_TRUSTEE, null, null, null).get();
	}

	@Test
	public void testBuildGetAttribRequestWorks() throws Exception {
		String raw = "endpoint";

		String expectedResult = String.format("\"identifier\":\"%s\"," +
				"\"operation\":{" +
				"\"type\":\"104\"," +
				"\"dest\":\"%s\"," +
				"\"raw\":\"%s\"" +
				"}", DID_TRUSTEE, DID_TRUSTEE, raw);

		String getAttribRequest = Ledger.buildGetAttribRequest(DID_TRUSTEE, DID_TRUSTEE, raw).get();

		assertTrue(getAttribRequest.contains(expectedResult));
	}

	@Test
	public void testSendAttribRequestWorksWithoutSignature() throws Exception {
		DidResults.CreateAndStoreMyDidResult trusteeDidResult = Did.createAndStoreMyDid(wallet, TRUSTEE_IDENTITY_JSON).get();
		String trusteeDid = trusteeDidResult.getDid();

		String attribRequest = Ledger.buildAttribRequest(trusteeDidResult.getDid(), trusteeDid, null, endpoint, null).get();
		String response = Ledger.submitRequest(pool, attribRequest).get();
		checkResponseType(response,"REQNACK" );
	}

	@Test
	public void testAttribRequestsWorks() throws Exception {
		DidResults.CreateAndStoreMyDidResult trusteeDidResult = Did.createAndStoreMyDid(wallet, TRUSTEE_IDENTITY_JSON).get();
		String trusteeDid = trusteeDidResult.getDid();

		DidResults.CreateAndStoreMyDidResult myDidResult = Did.createAndStoreMyDid(wallet, "{}").get();
		String myDid = myDidResult.getDid();
		String myVerkey = myDidResult.getVerkey();

		String nymRequest = Ledger.buildNymRequest(trusteeDid, myDid, myVerkey, null, null).get();
		Ledger.signAndSubmitRequest(pool, wallet, trusteeDid, nymRequest).get();

		String attribRequest = Ledger.buildAttribRequest(myDid, myDid, null, endpoint, null).get();
		Ledger.signAndSubmitRequest(pool, wallet, myDid, attribRequest).get();

		String getAttribRequest = Ledger.buildGetAttribRequest(myDid, myDid, "endpoint").get();
		String getAttribResponse = Ledger.submitRequest(pool, getAttribRequest).get();

		JSONObject getAttribResponseObject = new JSONObject(getAttribResponse);

		assertEquals(endpoint, getAttribResponseObject.getJSONObject("result").getString("data"));
	}

	@Test
	public void testBuildAttribRequestWorksForInvalidIdentifier() throws Exception {
		thrown.expect(ExecutionException.class);
		thrown.expectCause(isA(InvalidStructureException.class));

		Ledger.buildAttribRequest("invalid_base58_identifier", DID_TRUSTEE, null, endpoint, null).get();
	}
}