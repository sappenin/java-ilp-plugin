package org.interledger.lpiv2.blast;

import org.springframework.http.MediaType;

public interface BlastHeaders {

  String BLAST_AUDIENCE = "urn:ilpv4:blast";

  String ILP_OCTET_STREAM_VALUE = "application/ilp+octet-stream";
  String ILP_HEADER_OCTET_STREAM_VALUE = "application/ilp-header+octet-stream";
  MediaType ILP_OCTET_STREAM = MediaType.valueOf(ILP_OCTET_STREAM_VALUE);
  MediaType ILP_HEADER_OCTET_STREAM = MediaType.valueOf(ILP_HEADER_OCTET_STREAM_VALUE);

  String ILP_OPERATOR_ADDRESS_VALUE = "ILP-Operator-Address";
}
