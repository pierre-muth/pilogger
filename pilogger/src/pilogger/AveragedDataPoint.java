package pilogger;

public class AveragedDataPoint {
	public long time;
	public double value;
	public double min;
	public double max;

	public AveragedDataPoint(long time, double value, double min, double max) {
		this.time = time;
		this.value = value;
		this.max = max;
		this.min = min;
	}
}
