package com.googlecode.jmxtrans.cli;

import com.google.common.collect.ImmutableList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import java.io.File;

public class CliArgumentParser {
	/**
	 * Parse the options given on the command line.
	 *
	 * @param args
	 */
	public JmxTransConfiguration parseOptions(String[] args) throws OptionsException, org.apache.commons.cli.ParseException {
		CommandLineParser parser = new GnuParser();
		CommandLine cl = parser.parse(getOptions(), args);
		Option[] options = cl.getOptions();

		JmxTransConfiguration configuration = new JmxTransConfiguration();

		for (Option option : options) {
			
			if (option.getOpt().equals("c")) {
				configuration.setContinueOnJsonError(Boolean.parseBoolean(option.getValue()));
			} else if (option.getOpt().equals("es")) {
				configuration.setUseElasticsearch(true);
				configuration.setElasticsearchHost(option.getValue());
			} else if (option.getOpt().equals("j")) {
				if (configuration.isUseElasticsearch()) {
					throw new OptionsException("Incompatible options. Cannot use Elasticsearch and specify json directory.");
				}
				File jsonDir = new File(option.getValue());
				if (jsonDir.exists() && jsonDir.isDirectory()) {
					configuration.setJsonDirOrFile(jsonDir);
				} else {
					throw new OptionsException("Path to json directory is invalid: " + jsonDir);
				}
			} else if (option.getOpt().equals("f")) {
				if (configuration.isUseElasticsearch()) {
					throw new OptionsException("Incompatible options. Cannot use Elasticsearch and specify a json file.");
				}
				File jsonFile = new File(option.getValue());
				if (jsonFile.exists() && jsonFile.isFile()) {
					configuration.setJsonDirOrFile(jsonFile);
				} else {
					throw new OptionsException("Path to json file is invalid: " + jsonFile);
				}
			} else if (option.getOpt().equals("e")) {
				configuration.setRunEndlessly(true);
			} else if (option.getOpt().equals("q")) {
				File quartzConfigFile = new File(option.getValue());
				if (quartzConfigFile.exists() && quartzConfigFile.isFile()) {
					configuration.setQuartPropertiesFile(option.getValue());
				} else {
					throw new OptionsException("Could not find path to the quartz properties file: " + quartzConfigFile.getAbsolutePath());
				}
			} else if (option.getOpt().equals("s")) {
				try {
					configuration.setRunPeriod(Integer.parseInt(option.getValue()));
				} catch (NumberFormatException nfe) {
					throw new OptionsException("Seconds between server job runs must be an integer");
				}
			} else if (option.getOpt().equals("a")) {
				ImmutableList.Builder<File> jars = ImmutableList.builder();
				for (String jar : option.getValues()) {
					jars.add(new File(jar));
				}
				configuration.setAdditionalJars(jars.build());
			} else if (option.getOpt().equals("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar jmxtrans-all.jar", getOptions());
				configuration.setHelp(true);
			}
		}
		if ((!configuration.isHelp()) && (configuration.getJsonDirOrFile() == null) && (!configuration.isUseElasticsearch())) {
			throw new OptionsException("Please specify either the -f, -j or -es option.");
		}
		return configuration;
	}

	private Options getOptions() {
		Options options = new Options();
		options.addOption("c", true, "Continue processing even if one of the JSON configuration file is invalid.");
		options.addOption("es", true, "Use elasticsearch instead of files to store/read json configuration elements, specify elasticsearch host and port separating its with :");
		options.addOption("j", true, "Directory where json configuration is stored. Default is .");
		options.addOption("f", true, "A single json file to execute.");
		options.addOption("e", false, "Run endlessly. Default false.");
		options.addOption("q", true, "Path to quartz configuration file.");
		options.addOption("s", true, "Seconds between server job runs (not defined with cron). Default: 60");
		options.addOption(OptionBuilder.withArgName("a").withLongOpt("additionalJars").hasArgs().withValueSeparator(',')
				.withDescription("Coma delimited list of additional jars to add to the class path").create("a"));
		options.addOption("h", false, "Help");
		return options;
	}
}
