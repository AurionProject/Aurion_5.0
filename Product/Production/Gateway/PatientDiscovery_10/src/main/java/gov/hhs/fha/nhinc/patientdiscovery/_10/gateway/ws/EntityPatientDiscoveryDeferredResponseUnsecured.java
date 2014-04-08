/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.patientdiscovery._10.gateway.ws;

import gov.hhs.fha.nhinc.aspect.OutboundMessageEvent;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.entitypatientdiscoveryasyncresp.EntityPatientDiscoveryAsyncRespPortType;
import gov.hhs.fha.nhinc.messaging.server.BaseService;
import gov.hhs.fha.nhinc.patientdiscovery.aspect.MCCIIN000002UV01EventDescriptionBuilder;
import gov.hhs.fha.nhinc.patientdiscovery.aspect.RespondingGatewayPRPAIN201306UV02Builder;
import gov.hhs.fha.nhinc.patientdiscovery.outbound.deferred.response.OutboundPatientDiscoveryDeferredResponse;

import javax.annotation.Resource;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

import org.hl7.v3.MCCIIN000002UV01;
import org.hl7.v3.RespondingGatewayPRPAIN201306UV02RequestType;


@Addressing(enabled = true)
@BindingType(value = javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public class EntityPatientDiscoveryDeferredResponseUnsecured extends BaseService implements
        EntityPatientDiscoveryAsyncRespPortType {

    private OutboundPatientDiscoveryDeferredResponse outboundPatientDiscoveryResponse;

    private WebServiceContext context;

    public EntityPatientDiscoveryDeferredResponseUnsecured() {
        super();
    }

    @OutboundMessageEvent(beforeBuilder = RespondingGatewayPRPAIN201306UV02Builder.class,
            afterReturningBuilder =  MCCIIN000002UV01EventDescriptionBuilder.class, 
            serviceType = "Patient Discovery Deferred Response",
            version = "1.0")
    public MCCIIN000002UV01 processPatientDiscoveryAsyncResp(RespondingGatewayPRPAIN201306UV02RequestType request) {
        AssertionType assertion = getAssertion(context, request.getAssertion());

        return outboundPatientDiscoveryResponse.processPatientDiscoveryAsyncResp(request.getPRPAIN201306UV02(),
                assertion, request.getNhinTargetCommunities());
    }

    @Resource
    public void setContext(WebServiceContext context) {
        this.context = context;
    }

    public void setOutboundPatientDiscoveryResponse(
            OutboundPatientDiscoveryDeferredResponse outboundPatientDiscoveryResponse) {
        this.outboundPatientDiscoveryResponse = outboundPatientDiscoveryResponse;
    }

    /**
     * Gets the outbound patient discovery.
     *
     * @return the outbound patient discovery
     */
    public OutboundPatientDiscoveryDeferredResponse getOutboundPatientDiscovery() {
        return this.outboundPatientDiscoveryResponse;
    }

}