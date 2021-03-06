package io.hydrosphere.spark_ml_serving.common.classification

import io.hydrosphere.spark_ml_serving.common.{LocalData, LocalDataColumn, LocalPredictionModel}
import org.apache.spark.ml.classification.ClassificationModel
import org.apache.spark.ml.linalg.Vector

abstract class LocalClassificationModel[T <: ClassificationModel[Vector, T]]
  extends LocalPredictionModel[T] {
  def predictRaw(v: List[Double]): List[Double] = invokeVec('predictRaw, v)

  override def transform(localData: LocalData) = {
    localData.column(sparkTransformer.getFeaturesCol) match {
      case Some(column) =>
        var result = localData

        sparkTransformer.get(sparkTransformer.rawPredictionCol).foreach { name =>
          val res = LocalDataColumn(
            name,
            column.data.map(_.asInstanceOf[List[Double]]).map(predictRaw)
          )
          result = result.withColumn(res)
        }

        sparkTransformer.get(sparkTransformer.predictionCol).foreach { name =>
          val res =
            LocalDataColumn(name, column.data.map(_.asInstanceOf[List[Double]]).map(predict))
          result = result.withColumn(res)
        }

        result
      case None => localData
    }
  }
}
