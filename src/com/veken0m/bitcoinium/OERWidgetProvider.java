package com.veken0m.bitcoinium;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.StrictMode;
import android.widget.RemoteViews;

import com.xeiam.xchange.Currencies;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.oer.OERExchange;
import com.xeiam.xchange.service.marketdata.polling.PollingMarketDataService;

public class OERWidgetProvider extends BaseWidgetProvider {

	@Override
	public void onReceive(Context ctxt, Intent intent) {

		if (REFRESH.equals(intent.getAction())) {
			buildUpdate(ctxt);
		} else {
			super.onReceive(ctxt, intent);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
			buildUpdate(context);
	}
	
	public void buildUpdate(Context context){
		
		// TODO: MOVE NETWORKING TO SEPARATE THREAD
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		AppWidgetManager widgetManager = AppWidgetManager
				.getInstance(context);
		ComponentName widgetComponent = new ComponentName(context,
				OERWidgetProvider.class);
		int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);

		final Intent intent = new Intent(context, MainActivity.class);
		final PendingIntent pendingIntent = PendingIntent.getActivity(
				context, 0, intent, 0);

		for (int appWidgetId : widgetIds) {
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.oer_appwidget);

			views.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);

			try {

				// Use the factory to get the Open Exchange Rates exchange
				// API
				ExchangeSpecification exchangeSpecification = new ExchangeSpecification(
						OERExchange.class.getName());
				exchangeSpecification
						.setUri("http://openexchangerates.org");
				exchangeSpecification
						.setApiKey("ab32c922bca749ec9345b4717914ee1f");
				com.xeiam.xchange.Exchange openExchangeRates = ExchangeFactory.INSTANCE
						.createExchange(exchangeSpecification);

				// Interested in the polling market data feed
				PollingMarketDataService marketDataService = openExchangeRates
						.getPollingMarketDataService();

				// Get the latest ticker data showing CAD/USD
				String baseCurrency = Currencies.USD;
				String counterCurrency = Currencies.EUR;
				
				Ticker ticker = marketDataService.getTicker(
						counterCurrency,baseCurrency);

				// Retrieve values from ticker
				float lastValue = ticker.getLast().getAmount().floatValue();

				final String lastPrice = Utils.formatWidgetMoney(lastValue,
						counterCurrency, true);

				views.setTextViewText(R.id.oerBaseCurrency, baseCurrency);
				views.setTextViewText(R.id.oerCounterCurrency, lastPrice);

				views.setTextViewText(R.id.oer_time,
						"Refreshed @ " + Utils.getCurrentTime());
				views.setTextColor(R.id.oer_time, Color.GREEN);

			} catch (Exception e) {
				e.printStackTrace();
				if (pref_DisplayUpdates == true) {
					createTicker(context, R.drawable.bitcoin,
							"Open Exchange Rates" + " Update failed!");
				}
				views.setTextColor(R.id.oer_time, Color.RED);
			}
			widgetManager.updateAppWidget(appWidgetId, views);
			// }
		}
	}
	
	
}
