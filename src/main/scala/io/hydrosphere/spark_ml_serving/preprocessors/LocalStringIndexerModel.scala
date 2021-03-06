package io.hydrosphere.spark_ml_serving.preprocessors

import io.hydrosphere.spark_ml_serving.TypedTransformerConverter
import io.hydrosphere.spark_ml_serving.common._
import org.apache.spark.SparkException
import org.apache.spark.ml.feature.StringIndexerModel

import scala.collection.mutable

class LocalStringIndexerModel(override val sparkTransformer: StringIndexerModel)
  extends LocalTransformer[StringIndexerModel] {
  override def transform(localData: LocalData): LocalData = {
    localData.column(sparkTransformer.getInputCol) match {
      case Some(column) =>
        val labelToIndex = {
          val n   = sparkTransformer.labels.length
          val map = new mutable.HashMap[String, Double]
          var i   = 0
          while (i < n) {
            map.update(sparkTransformer.labels(i), i)
            i += 1
          }
          map
        }
        val indexer = (label: String) => {
          if (labelToIndex.contains(label)) {
            labelToIndex(label)
          } else {
            throw new SparkException(s"Unseen label: $label.")
          }
        }
        val newColumn =
          LocalDataColumn(sparkTransformer.getOutputCol, column.data.map(_.toString) map {
            feature =>
              indexer(feature)
          })
        localData.withColumn(newColumn)
      case None => localData
    }
  }
}

object LocalStringIndexerModel
  extends SimpleModelLoader[StringIndexerModel]
  with TypedTransformerConverter[StringIndexerModel] {

  override def build(metadata: Metadata, data: LocalData): StringIndexerModel = {
    new StringIndexerModel(
      metadata.uid,
      data.column("labels").get.data.head.asInstanceOf[Seq[String]].toArray
    ).setInputCol(metadata.paramMap("inputCol").asInstanceOf[String])
      .setOutputCol(metadata.paramMap("outputCol").asInstanceOf[String])
      .setHandleInvalid(metadata.paramMap("handleInvalid").asInstanceOf[String])
  }

  override implicit def toLocal(
    transformer: StringIndexerModel
  ) = new LocalStringIndexerModel(transformer)
}
