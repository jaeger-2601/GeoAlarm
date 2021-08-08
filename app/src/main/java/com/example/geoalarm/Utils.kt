package com.example.geoalarm

import com.example.geoalarm.data.Alarm

enum class OPERATIONS {
    NONE,
    ADD,
    DELETE,
    REPLACE
}

data class DiffedElement(val op: OPERATIONS, val i: Int = -1, val j: Int = -1)

fun diffMarkers(a1: List<Alarm>, a2: List<Alarm>): List<DiffedElement> {

    // Highly specific form of diffing algorithm that only works on lists containing unique elements


    var compMatrix: MutableList<MutableList<Int>> = mutableListOf()
    var row_sums: MutableList<Int> = mutableListOf()
    var col_sums: MutableList<Int> = mutableListOf()
    var diff_list: MutableList<DiffedElement> = mutableListOf()
    var sum = 0;
    var i = 0;
    var j = 0;

    //Generate comparison matrix
    for (alarm1 in a1) {

        compMatrix.add(mutableListOf())

        for (alarm2 in a2)
            compMatrix.last().add(if (alarm1 == alarm2) 1 else 0)
    }

    // Generate row sums
    for (list in compMatrix){
        row_sums.add(list.sum())
    }

    // Generate column sums
    for (a in 0..compMatrix[0].lastIndex){
        sum = 0;
        for (b in 0..compMatrix.lastIndex){
            sum += compMatrix[a][b];
        }
        col_sums.add(sum);
    }

    while (!(i == a1.size || j == a2.size)){

        if (!(i == a1.size && j == a2.size) && col_sums[j] == row_sums[i] ){

            if (row_sums[i] == 1){
                diff_list.add(DiffedElement(OPERATIONS.NONE))
            }
            else{
                diff_list.add(DiffedElement(OPERATIONS.REPLACE, i, j))
            }

            i++;j++;
        }
        else if (i != a1.size && row_sums[i] == 0){
            diff_list.add(DiffedElement(OPERATIONS.DELETE, i))
            i++
        }
        else if (j != a2.size && col_sums[j] == 0){
            diff_list.add(DiffedElement(OPERATIONS.ADD, j = j))
            j++
        }
    }

    return diff_list

}

