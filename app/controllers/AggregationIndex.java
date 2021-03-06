package controllers;

import models.Record;
import models.Resource;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import play.Logger;
import play.mvc.Result;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fo
 */
public class AggregationIndex extends OERWorldMap {

  public static Result list() throws IOException {
    @SuppressWarnings("rawtypes")

    String resource_field = Record.RESOURCEKEY + ".location.address.addressCountry";
    String mentions_field = Record.RESOURCEKEY + ".mentions.location.address.addressCountry";
    String provider_field = Record.RESOURCEKEY + ".provider.location.address.addressCountry";
    String participant_field = Record.RESOURCEKEY + ".participant.location.address.addressCountry";

    AggregationBuilder byCountry = AggregationBuilders
        .terms("by_country").script("doc['"
            + resource_field + "'].values + doc['" + mentions_field + "'].values  + doc['"
            + provider_field + "'].values  + doc['" + participant_field + "'].values").size(0)
        .subAggregation(AggregationBuilders
            .filter("organizations")
            .filter(FilterBuilders.termFilter(Record.RESOURCEKEY + ".@type", "Organization")))
        .subAggregation(AggregationBuilders
            .filter("users")
            .filter(FilterBuilders.termFilter(Record.RESOURCEKEY + ".@type", "Person")))
        .subAggregation(AggregationBuilders
            .filter("articles")
            .filter(FilterBuilders.termFilter(Record.RESOURCEKEY + ".@type", "Article")))
        .subAggregation(AggregationBuilders
            .filter("services")
            .filter(FilterBuilders.termFilter(Record.RESOURCEKEY + ".@type", "Service")))
        .subAggregation(AggregationBuilders
            .filter("projects")
            .filter(FilterBuilders.termFilter(Record.RESOURCEKEY + ".@type", "Action")))
        // TODO: The following implies that somebody can be a champion for another country. Is this correct?
        .subAggregation(AggregationBuilders
            .filter("champions")
            .filter(FilterBuilders.existsFilter(Record.RESOURCEKEY + ".countryChampionFor")));
    Resource countryAggregation = mBaseRepository.query(byCountry);
    Map<String,Object> scope = new HashMap<>();
    scope.put("countryAggregation", countryAggregation);
    return ok(render("Country Aggregations", "AggregationIndex/index.mustache", scope));
  }

}
