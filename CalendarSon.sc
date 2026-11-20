CalendarSon { // : Object is implied
	classvar toSecsDict, <>testVar;

	*initClass {
        toSecsDict = Dictionary.newFrom([minute: 60, hour: 60*60, day: 24*60*60, week:7*24*60*60, fortnight:14*24*60*60, year: 365*24*60*60]);
        testVar = 42;
    }

	*toSecs { |timeSpan| //One of the symbols \minute, \hour, \day, \week, \fortnight, \year
        ^toSecsDict[timeSpan];
	}

	*readDatetimes { |filepath|
		var datetimes = CSVFileReader.read(filepath,
			skipEmptyLines:true, skipBlanks:true);
		var col_names = datetimes.removeAt(0); //column headings.
		col_names = col_names.collect({|item, idx|
			if (item == "seconds_to_next") {
				\dur;
			} {
				item.asSymbol;
			}
		});
		datetimes = datetimes.collect({|row, idx|
			[row[0].asInteger, row[1].asInteger, row[2].asInteger, row[3].asInteger,
				row[4].asInteger, row[5].asInteger, row[6].asInteger, row[7].asInteger,
				row[8].asInteger, row[9], row[10], row[11]]
		});
		^[col_names, datetimes];
	}

	*readSunrises { |filepath|
		var sunrises = CSVFileReader.read(filepath,
			skipEmptyLines:true, skipBlanks:true);
		var col_names = sunrises.removeAt(0); //column headings.
		col_names = col_names.collect({|item, idx|
			if (item == "seconds_to_next") {
				\dur;
			} {
				item.asSymbol;
			}
		});
		sunrises = sunrises.collect({|row, idx|
			[row[0], row[1].asInteger, row[2].asInteger, row[3].asInteger, row[4].asInteger]
		});
		^[col_names, sunrises];
	}

	*makeDaysBind { |filepath, sunBus|
		var keys, days, firstJan, daysSeq, daysBind;

		#keys, days = this.readDatetimes(filepath);
		keys.postln;
		firstJan = days[0][6].postln;//New Year's Day in raw seconds
		postln("Everydays size: " + days.size);
		daysSeq = Pseq(days);
		daysBind = Pbind(
			\instrument, \daySon,
			\in, sunBus,
			\amp, 0.2,
			\timeScale, Pfunc({ thisThread.clock.beatDur }),
			\dur, this.toSecs(\day),
			keys, daysSeq
		);
		^[firstJan, daysBind];
	}

	*makeSamplesBind { |filepath, firstJan|
		var keys, samples, samplesSeq, daysSeq, secsToFirstSample, restToFirstSample, samplesBind;
		#keys, samples = this.readDatetimes(filepath);
		keys.postln;
		postln("Datetimes size: " + samples.size);
		secsToFirstSample = samples[0][6] - firstJan; //samples[0][6] indexes the raw_seconds field of the first sample row
		restToFirstSample = [0,0,0,0,0,0,0, Rest(secsToFirstSample), 0, "None", "None", "None"]; // \dur key will map to the rest
		postf("Samples[0] size: %. restToFirstSample size: %\n", samples[0].size, restToFirstSample.size);
		restToFirstSample.postln;
		samplesSeq = Pseq([restToFirstSample] ++ samples);
		samples[0].postln;

		^samplesBind = Pbind(
			\instrument, \sampleSon,
			\sampleLength, 60, //each sample is a wav 60 seconds long
			\timeScale, Pfunc({ thisThread.clock.beatDur }),
			keys, samplesSeq;
		);
	}

	*makeSunrisesBind {|filepath, firstJan, sunBus|
		var keys, sunrises, secsToFirstSunrise, restToFirstSunrise, sunrisesSeq, sunrisesBind;
		#keys, sunrises = this.readSunrises(filepath);
		sunrises.postln;
		keys.postln;
		secsToFirstSunrise = sunrises[0][1] - firstJan;
		restToFirstSunrise = ["None", 0, 0, 0, Rest(secsToFirstSunrise)]; // rest will map to the \dur key
		sunrisesSeq = Pseq([restToFirstSunrise] ++ sunrises);
		^sunrisesBind = Pbind(
			\instrument, \sunSon,
			\addAction, \addToHead,
			\out, sunBus,
			\amp, 0.6,
			\timeScale, Pfunc({ thisThread.clock.beatDur }),
			keys, sunrisesSeq;
		);
	}
}




