/**
 *
 */
package playground.yu.analysis;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.matsim.analysis.VolumesAnalyzer;
import org.matsim.events.Events;
import org.matsim.events.MatsimEventsReader;
import org.matsim.gbl.Gbl;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.population.MatsimPopulationReader;
import org.matsim.population.PopulationImpl;
import org.matsim.utils.charts.XYLineChart;
import org.matsim.utils.io.IOUtils;

/**
 * @author yu
 *
 */
public class LinkTrafficVolumeExtractor {
	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		int timeBin = 60;
		final String netFilename = "../psrc/network/psrc-wo-3212.xml.gz";
		final String plansFilename = "../runs/run668/it.1500/1500.plans.xml.gz";
		final String eventsFilename = "../runs/run668/it.1500/1500.analysis/6760.events.txt";
		final String outFilename = "../runs/run668/it.1500/1500.analysis/6760.volume.";

		Gbl.startMeasurement();
		Gbl.createConfig(null);

		NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(netFilename);

		Population population = new PopulationImpl();
		System.out.println("-->reading plansfile: " + plansFilename);
		new MatsimPopulationReader(population, network).readFile(plansFilename);

		Events events = new Events();

		VolumesAnalyzer va = new VolumesAnalyzer(timeBin, 30 * 3600, network);
		events.addHandler(va);

		System.out.println("-->reading evetsfile: " + eventsFilename);
		new MatsimEventsReader(events).readFile(eventsFilename);

		BufferedWriter writer;
		double[] xs = new double[(24 * 3600 + 1) / timeBin];
		double[] ys = new double[(24 * 3600 + 1) / timeBin];
		int index;
		try {
			writer = IOUtils.getBufferedWriter(outFilename + timeBin + ".txt");
			writer.write("TimeBin\tLinkVolume[veh/" + timeBin
					+ " s]\tLinkVolume[veh/h]\n");
			for (int anI = 0; anI < 24 * 3600; anI = anI + timeBin) {
				index = (anI) / timeBin;
				ys[index] = va.getVolumesForLink("6760")[index];
				writer.write(anI + "\t" + ys[index] + "\t" + ys[index] * 3600.0
						/ timeBin + "\n");
				ys[index] = ys[index] * 3600.0 / timeBin;
				xs[index] = (anI) / 3600.0;
				writer.flush();
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		XYLineChart travelTimeChart = new XYLineChart(
				"Volumes of Link 6760 of psrc network", "time [h]",
				"Volume [veh/h]");
		travelTimeChart.addSeries("with timeBin " + timeBin / 60 + " min.", xs,
				ys);
		travelTimeChart.saveAsPng(outFilename + timeBin + ".png", 1024, 768);

		System.out.println("--> Done!");
		Gbl.printElapsedTime();
		System.exit(0);
	}

}
