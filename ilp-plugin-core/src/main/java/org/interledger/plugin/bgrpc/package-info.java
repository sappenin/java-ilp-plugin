/**
 * <p>This package holds all classes and interfaces necessary to implement the Bilateral gRPC Protocol (bgrpc), which
 * allows two ILPv4 nodes to communicate over one or more bilateral accounts identified by an Interledger Address.</p>
 *
 * <p>Unlike LPIv2, this protocol supports multiple accounts over the same mux, allowing a single gRPC connection
 * to mux ILP packets for multiple accounts at the same time.</p>
 */

package org.interledger.plugin.bgrpc;