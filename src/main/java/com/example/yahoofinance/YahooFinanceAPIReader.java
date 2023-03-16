package com.example.yahoofinance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.DependencyManager;
import com.snaplogic.common.properties.SnapProperty;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SimpleSnap;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.SnapDataException;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

@General(title = "Yahoo Finance API Reader", author = "Your Company Name",
        purpose = "Retrieve current stock price for a given ticker symbol using Yahoo Finance API",
        docLink = "http://yourdocslinkhere.com")
@Inputs(min = 0, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class YahooFinanceAPIReader extends SimpleSnap { //implements DependencyManager {
    private static final String TICKER_SYMBOL_PROP = "tickerSymbol";

    private String tickerSymbol;
    private YahooFinanceAPI yahooFinanceAPI;

    public YahooFinanceAPIReader() {
        this.yahooFinanceAPI = new YahooFinanceAPIImpl(new ObjectMapper());
    }

    // @Inject
    // public YahooFinanceAPIReader(YahooFinanceAPI yahooFinanceAPI) {
    //     this.yahooFinanceAPI = yahooFinanceAPI;
    // }

    // @Override
    // public Module getManagedModule() {
    //     return new AbstractModule() {
    //         @Override
    //         protected void configure() {
    //             bind(YahooFinanceAPI.class).to(YahooFinanceAPIImpl.class);
    //         }
    //     };
    // }

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(TICKER_SYMBOL_PROP, "Ticker Symbol",
                "The stock ticker symbol to fetch the current price")
                .expression()
                .required()
                .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        tickerSymbol = propertyValues.get(TICKER_SYMBOL_PROP);
    }

    @Override
    protected void process(Document document, String inputViewName) {
        try {
            Map<String, Object> stockPriceData = yahooFinanceAPI.getStockPrice(tickerSymbol);
            if (stockPriceData != null) {
                outputViews.write(documentUtility.newDocument(stockPriceData));
            }
        } catch (IOException e) {
            throw new SnapDataException(e, "Error retrieving stock price data from Yahoo Finance API");
        }
    }

    public interface YahooFinanceAPI {
        Map<String, Object> getStockPrice(String tickerSymbol) throws IOException;
    }

    public static class YahooFinanceAPIImpl implements YahooFinanceAPI {
        private static final String YAHOO_FINANCE_API_URL = "https://query1.finance.yahoo.com/v11/finance/quoteSummary/%s?modules=financialData";
        private ObjectMapper mapper;

        @Inject
        public YahooFinanceAPIImpl(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public Map<String, Object> getStockPrice(String tickerSymbol) throws IOException {
            String url = String.format(YAHOO_FINANCE_API_URL, tickerSymbol);
            Map<String, Object> stockPriceData;
            try {
                stockPriceData = mapper.readValue(new URL(url), Map.class);
            } catch (IOException e) {
                throw new IOException("Error fetching data from Yahoo Finance API", e);
            }

            Map<String, Object> quoteSummary = (Map<String, Object>) stockPriceData.get("quoteSummary");
            if (quoteSummary != null) {
                List<Map<String, Object>> result = (List<Map<String, Object>>) quoteSummary.get("result");
                if (result != null && !result.isEmpty()) {
                    Map<String, Object> metaData = result.get(0);
                    Map<String, Object> financialData = (Map<String, Object>) metaData.get("financialData");
                    if (financialData != null) {
                        return financialData;
                    }
                }
            }

            return null;
        }
    }
}
