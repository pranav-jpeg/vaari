package com.vaari.app.ui.result

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.vaari.app.R
import com.vaari.app.model.CropResponse

class ResultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_result, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val crop = arguments?.getParcelable<CropResponse>("cropResult") ?: return

        view.findViewById<TextView>(R.id.tvCropName).text = crop.productName
        view.findViewById<TextView>(R.id.tvTotalWater).text =
            "${String.format("%.0f", crop.totalWater)} ${crop.unit}"
        view.findViewById<TextView>(R.id.tvGreenWater).text =
            "${String.format("%.0f", crop.greenWater)} L/kg"
        view.findViewById<TextView>(R.id.tvBlueWater).text =
            "${String.format("%.0f", crop.blueWater)} L/kg"
        view.findViewById<TextView>(R.id.tvGreyWater).text =
            "${String.format("%.0f", crop.greyWater)} L/kg"
        view.findViewById<TextView>(R.id.tvWaterTip).text = crop.waterSavingTip

        view.findViewById<TextView>(R.id.tvClimate).text =
            getString(R.string.label_climate, crop.climate)
        view.findViewById<TextView>(R.id.tvIrrigation).text =
            getString(R.string.label_irrigation, crop.irrigationType)
        view.findViewById<TextView>(R.id.tvScarcity).text =
            getString(R.string.label_scarcity, crop.waterScarcity)
        view.findViewById<TextView>(R.id.tvSeason).text =
            getString(R.string.label_season, crop.harvestSeason)

        setupPieChart(
            view,
            crop.greenWater.toFloat(),
            crop.blueWater.toFloat(),
            crop.greyWater.toFloat()
        )
    }

    private fun setupPieChart(view: View, green: Float, blue: Float, grey: Float) {
        val chart = view.findViewById<PieChart>(R.id.pieChart)

        val entries = mutableListOf<PieEntry>()
        if (green > 0) entries.add(PieEntry(green, getString(R.string.water_green)))
        if (blue > 0) entries.add(PieEntry(blue, getString(R.string.water_blue)))
        if (grey > 0) entries.add(PieEntry(grey, getString(R.string.water_grey)))

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#9E9E9E")
            )
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(chart)
        }

        chart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 42f
            setHoleColor(Color.WHITE)
            centerText = getString(R.string.water_footprint)
            setCenterTextSize(13f)
            setCenterTextColor(Color.parseColor("#37474F"))
            legend.isEnabled = true
            setUsePercentValues(true)
            animateY(1000)
            invalidate()
        }
    }
}