package no.sikt.nva.pubchannels.handler.create.series;

public record CreateSeriesRequest(String name,
                                  String printIssn,
                                  String onlineIssn,
                                  String homepage) {

}
