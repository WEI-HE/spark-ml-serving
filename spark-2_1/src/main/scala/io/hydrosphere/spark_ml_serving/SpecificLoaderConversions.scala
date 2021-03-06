package io.hydrosphere.spark_ml_serving

import io.hydrosphere.spark_ml_serving.classification._
import io.hydrosphere.spark_ml_serving.common._
import io.hydrosphere.spark_ml_serving.preprocessors.LocalWord2VecModel
import org.apache.spark.ml.classification._
import org.apache.spark.ml.feature.Word2VecModel

object SpecificLoaderConversions extends DynamicLoaderConverter {
  implicit def sparkToLocal(m: Any): ModelLoader[_] = {
    m match {
      case _: LogisticRegressionModel.type => LocalLogisticRegressionModel
      case _: Word2VecModel.type           => LocalWord2VecModel
      case _                               => throw new Exception(s"Unknown model: ${m.getClass}")
    }
  }
}
