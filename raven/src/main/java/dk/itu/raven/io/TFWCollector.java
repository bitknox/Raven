package dk.itu.raven.io;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class TFWCollector implements Collector<TFWFormat, TFWFormat, TFWFormat> {

	@Override
	public Supplier<TFWFormat> supplier() {
		return TFWFormat::new;
		// accumulator
	}

	@Override
	public BiConsumer<TFWFormat, TFWFormat> accumulator() {
		return (accumulator, tfwFormat) -> {
			if (accumulator == null) {
				accumulator = tfwFormat;
			} else {
				double accumulatedTopLeft = accumulator.topLeftX + accumulator.topLeftY;
				double currentTopLeft = tfwFormat.topLeftX + tfwFormat.topLeftY;
				if (currentTopLeft < accumulatedTopLeft) {
					accumulator = tfwFormat;
				}
			}
		};
	}

	@Override
	public BinaryOperator<TFWFormat> combiner() {
		return (accumulator1, accumulator2) -> {
			double accumulatedTopLeft1 = accumulator1.topLeftX + accumulator1.topLeftY;
			double accumulatedTopLeft2 = accumulator2.topLeftX + accumulator2.topLeftY;
			return (accumulatedTopLeft1 < accumulatedTopLeft2) ? accumulator1 : accumulator2;
		};
	}

	@Override
	public Function<TFWFormat, TFWFormat> finisher() {
		return Function.identity(); // The identity function, as the result is already of the desired type
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Set.of(); // No special characteristics for this collector
	}
}
