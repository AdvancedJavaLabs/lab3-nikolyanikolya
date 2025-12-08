package lab3;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

public class WordCount {

  public static class PrettyOutputFormat extends FileOutputFormat<Statistic, Text> {

    @Override
    public RecordWriter<Statistic, Text> getRecordWriter(TaskAttemptContext ctx) throws IOException {
      Path path = FileOutputFormat.getOutputPath(ctx);
      Path fullPath = new Path(path, "results.txt");
      FileSystem fs = path.getFileSystem(ctx.getConfiguration());
      FSDataOutputStream fileOut = fs.create(fullPath, ctx);

      return new PrettyRecordWriter(fileOut);
    }

    public static class PrettyRecordWriter extends RecordWriter<Statistic, Text> {

      private final FSDataOutputStream out;
      private boolean headerWritten = false;

      public PrettyRecordWriter(FSDataOutputStream out) {
        this.out = out;
      }

      @Override
      public void write(Statistic key, Text value) throws IOException {

        if (!headerWritten) {
          String header = String.format("%-32s|%-25s|%s\n", "Category", "Revenue", "Quantity");
          out.write(header.getBytes());
          out.write(("-".repeat(70) + "\n").getBytes());
          headerWritten = true;
        }

        String formatted = String.format("%-32s|%-25.2f|%d\n", value.toString(), key.getRevenue(), key.getQuantity());

        out.write(formatted.getBytes());
      }

      @Override
      public void close(TaskAttemptContext context) throws IOException {
        out.close();
      }
    }
  }


  public static class CsvOutputFormat extends FileOutputFormat<Text, Statistic> {

    @Override
    public RecordWriter<Text, Statistic> getRecordWriter(TaskAttemptContext ctx) throws IOException {
      Path path = FileOutputFormat.getOutputPath(ctx);
      Path fullPath = new Path(path, "intermediate.txt");
      FileSystem fs = path.getFileSystem(ctx.getConfiguration());
      FSDataOutputStream fileOut = fs.create(fullPath, ctx);

      return new CsvRecordWriter(fileOut);
    }

    public static class CsvRecordWriter extends RecordWriter<Text, Statistic> {

      private final FSDataOutputStream out;

      public CsvRecordWriter(FSDataOutputStream out) {
        this.out = out;
      }

      @Override
      public void write(Text key, Statistic value) throws IOException {
        String formatted = String.format("%s,%.2f,%d\n", key.toString(), value.getRevenue(), value.getQuantity());

        out.write(formatted.getBytes());
      }

      @Override
      public void close(TaskAttemptContext context) throws IOException {
        out.close();
      }
    }
  }

  public static class TokenizerMapper extends Mapper<Object, Text, Text, Statistic> {

    private Text word = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String[] fields = value.toString().split(",");
      if (Objects.equals(fields[0], "transaction_id")) {
        return;
      }
      Integer quantity = Integer.parseInt(fields[4]);
      String category = fields[2];
      Double revenue = Double.parseDouble(fields[3]);

      word.set(category);

      context.write(word, new Statistic(quantity, revenue));
    }
  }

  public static class IntSumReducer extends Reducer<Text, Statistic, Text, Statistic> {

    public void reduce(Text key, Iterable<Statistic> values, Context context) throws IOException, InterruptedException {
      double totalRevenue = 0;
      int totalQuantity = 0;
      for (Statistic val : values) {
        totalRevenue += val.getRevenue();
        totalQuantity += val.getQuantity();
      }

      context.write(key, new Statistic(totalQuantity, totalRevenue));
    }
  }

  public static class SortingMapper extends Mapper<Object, Text, Statistic, Text> {

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String[] fields = value.toString().split(",");
      String category = fields[0];
      Double revenue = Double.parseDouble(fields[1]);
      Integer quantity = Integer.parseInt(fields[2]);

      context.write(new Statistic(quantity, revenue), new Text(category));
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    deleteRecursively(args[1]);
    deleteRecursively(args[2]);
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(WordCount.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Statistic.class);
    job.setMapOutputValueClass(Statistic.class);
    job.setMapOutputKeyClass(Text.class);
    job.setOutputFormatClass(CsvOutputFormat.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    if (!job.waitForCompletion(true)) {
      System.exit(1);
    }

    Configuration conf2 = new Configuration();
    Job sorterJob = Job.getInstance(conf2, "sorter");
    sorterJob.setJarByClass(WordCount.class);
    sorterJob.setMapperClass(SortingMapper.class);
    sorterJob.setOutputKeyClass(Statistic.class);
    sorterJob.setOutputValueClass(Text.class);
    sorterJob.setMapOutputKeyClass(Statistic.class);
    sorterJob.setMapOutputValueClass(Text.class);
    sorterJob.setOutputFormatClass(PrettyOutputFormat.class);
    FileInputFormat.addInputPath(sorterJob, new Path(args[1]));
    FileOutputFormat.setOutputPath(sorterJob, new Path(args[2]));
    if (!sorterJob.waitForCompletion(true)) {
      System.exit(1);
    }
    System.exit(0);
  }

  private static void deleteRecursively(String dirname) throws IOException {
    var dirPath = java.nio.file.Path.of(dirname);
    if (Files.exists(dirPath)) {
      Arrays.stream(Objects.requireNonNull(dirPath.toFile().listFiles())).forEach(f -> {
        try {
          Files.delete(f.toPath());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
      Files.delete(dirPath);
    }
  }
}
