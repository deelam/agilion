package dataengine.workers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.apis.OperationConsts;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.deelam.vertx.jobboard.JobDTO;
import net.deelam.vertx.jobboard.ProgressState;
import net.deelam.vertx.jobboard.ProgressingDoer;

@Accessors(fluent = true)
public class IngestPeopleWorker implements Worker_I, ProgressingDoer {

  @Getter
  private String name = "IngestPeopleWorker-" + System.currentTimeMillis();

  @Getter
  public String jobType = "INGEST_SOURCE_DATASET";

  @Getter
  public ProgressState state = new ProgressState();

  @Getter
  public Collection<Operation> operations = new ArrayList<>();

  {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_INGESTER);
    operations.add(new Operation()
        .id("INGEST_SOURCE_DATASET")
        .description("add source dataset 2")
        .info(info)
        .addParamsItem(new OperationParam()
            .key("inputUri").required(true)
            .description("location of source dataset")
            .valuetype(ValuetypeEnum.STRING).isMultivalued(false)
            .defaultValue(null))
        .addParamsItem(new OperationParam()
            .key(OperationConsts.INGEST_DATAFORMAT).required(false)
            .description("type and format of data 2")
            .valuetype(ValuetypeEnum.ENUM).isMultivalued(true)
            .defaultValue(null)
            .addPossibleValuesItem("PEOPLE.CSV"))
    //
    );
  }

  @Override
  public void accept(JobDTO t) {
    // TODO Auto-generated method stub

  }

}
