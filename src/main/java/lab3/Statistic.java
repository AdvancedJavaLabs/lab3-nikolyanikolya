package lab3;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class Statistic implements WritableComparable<Statistic> {
  private int quantity;
  private double revenue;

  public Statistic(Integer quantity, Double revenue) {
    this.quantity = quantity;
    this.revenue = revenue;
  }

  public Statistic() {
  }

  public Double getRevenue() {
    return revenue;
  }

  public Integer getQuantity() {
    return quantity;
  }

  @Override
  public String toString() {
    return String.format("%15.2f %10d", revenue, quantity);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Statistic statistic = (Statistic) o;
    return Objects.equals(quantity, statistic.quantity) && Objects.equals(revenue, statistic.revenue);
  }

  @Override
  public int compareTo(Statistic o) {
    var revenueComparison = Double.compare(o.revenue, revenue);
    if (revenueComparison == 0) {
      return Integer.compare(o.quantity, quantity);
    }
    return revenueComparison;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(quantity);
    out.writeDouble(revenue);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    quantity = in.readInt();
    revenue = in.readDouble();
  }
}
