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
package gov.hhs.fha.nhinc.docsubmission._20.nhin.deferred.response;

import gov.hhs.fha.nhinc.aspect.InboundMessageEvent;
import gov.hhs.fha.nhinc.docsubmission.aspect.RegistryResponseTypeHolderBuilder;
import gov.hhs.fha.nhinc.docsubmission.inbound.deferred.response.InboundDocSubmissionDeferredResponse;
import gov.hhs.fha.nhinc.event.DefaultEventDescriptionBuilder;

import javax.annotation.Resource;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

@BindingType(value = javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
@Addressing(enabled = true)
public class NhinXDRResponse20 implements ihe.iti.xdr._2007.XDRDeferredResponse20PortType {

    private WebServiceContext context;
    private InboundDocSubmissionDeferredResponse inboundDocSubmissionResponse;

    @Override
    @InboundMessageEvent(beforeBuilder = RegistryResponseTypeHolderBuilder.class,
            afterReturningBuilder = DefaultEventDescriptionBuilder.class,
            serviceType = "Document Submission Deferred Response", version = "2.0")
    public void provideAndRegisterDocumentSetBDeferredResponse(javax.xml.ws.Holder<RegistryResponseType> body) {
        body.value = new NhinDocSubmissionDeferredResponseImpl20(inboundDocSubmissionResponse)
                .provideAndRegisterDocumentSetBResponse(body.value, context);
    }

    public void setInboundDocSubmissionResponse(InboundDocSubmissionDeferredResponse inboundDocSubmissionResponse) {
        this.inboundDocSubmissionResponse = inboundDocSubmissionResponse;
    }

    @Resource
    public void setContext(WebServiceContext context) {
        this.context = context;
    }

    /**
     * Gets the inbound doc submission.
     *
     * @return the inbound doc submission
     */
    public InboundDocSubmissionDeferredResponse getInboundDocSubmission() {
        return this.inboundDocSubmissionResponse;
    }
}