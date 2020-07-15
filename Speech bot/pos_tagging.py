# File: pos_tagging.py
# Template file for Informatics 2A Assignment 2:
# 'A Natural Language Query System in Python/NLTK'

# John Longley, November 2012
# Revised November 2013 and November 2014 with help from Nikolay Bogoychev
# Revised November 2015 by Toms Bergmanis


# PART B: POS tagging

from statements import *

# The tagset we shall use is:
# P  A  Ns  Np  Is  Ip  Ts  Tp  BEs  BEp  DOs  DOp  AR  AND  WHO  WHICH  ?

# Tags for words playing a special role in the grammar:

function_words_tags = [('a','AR'), ('an','AR'), ('and','AND'),
     ('is','BEs'), ('are','BEp'), ('does','DOs'), ('do','DOp'),
     ('who','WHO'), ('which','WHICH'), ('Who','WHO'), ('Which','WHICH'), ('?','?')]
     # upper or lowercase tolerated at start of question.

function_words = [p[0] for p in function_words_tags]

def unchanging_plurals():
    nns = []
    nn = []

    with open("sentences.txt", "r") as f:
        for line in f:
            for pair in line.split():
                word, tag = pair.split('|')
                if tag == 'NN':
                    nn.append(word)
                elif tag == 'NNS':
                    nns.append(word)

    unchanging = [word for word in (set(nn) & set(nns))]

    return unchanging


unchanging_plurals_list = unchanging_plurals()

def noun_stem (s):
    """extracts the stem from a plural noun, or returns empty string"""
    # add code here
    if s in unchanging_plurals_list:
        return s
    elif re.match(r'.*men', s):
        return s[:-2] + 'an'
    else:
        stem = ""
        if s == 'has':
            stem = 'have'
        elif s == 'unties':
            stem = 'untie'
        elif re.match(r'[^aeiou]ies', s, re.IGNORECASE):
            stem = s[:-1]
        elif re.match(r'.*[^aeiou]ies', s, re.IGNORECASE):
            stem = s[:-3] + 'y'
        elif re.match(r'.*(o|x|(ch)|(sh)|(ss)|(zz))es', s, re.IGNORECASE):
            stem = s[:-2]
        elif re.match(r'.*[^iosxz]es', s, re.IGNORECASE) and not re.match(r'.*ches', s, re.IGNORECASE) and not re.match(
                r'.*shes', s, re.IGNORECASE):
            stem = s[:-1]
        elif re.match('.*[^i]es', s, re.IGNORECASE):
            stem = s[:-1]
        elif re.match(r'(.*[^s]ses)|(.*[^z]zes)', s, re.IGNORECASE):
            stem = s[:-1]
        elif re.match(r'.*[aeiou]ys', s, re.IGNORECASE):
            stem = s[:-1]
        elif re.match(r'.*[^aeiousxyz]s', s, ) and not re.match(r'.*ches', s) and not re.match(r'.*shes', s):
            stem = s[:-1]
        else:
            stem = ""
        return stem


def tag_word (lx,wd):
    """returns a list of all possible tags for wd relative to lx"""
    # add code here
    tags = []
    noun = noun_stem(wd)
    verb = verb_stem(wd)
    if wd in function_words:
        return [x[1] for x in function_words_tags if x[0] == wd]
    for tag in ['P', 'A']:
        if wd in lx.getAll(tag):
            tags.append(tag)
    for tag in ['I', 'T']:
        if (wd in lx.getAll(tag)) or (verb in lx.getAll(tag)):
            if verb != wd:
                tags.append(tag +'s')
            else:
                tags.append(tag +'p')
    if wd in lx.getAll('N'):
        if wd in unchanging_plurals_list:
            tags.append('Ns')
            tags.append('Np')
        elif noun == "":
            tags.append('Ns')
        else:
            tags.append('Np')

    return tags

def tag_words (lx, wds):
    """returns a list of all possible taggings for a list of words"""
    if (wds == []):
        return [[]]
    else:
        tag_first = tag_word (lx, wds[0])
        tag_rest = tag_words (lx, wds[1:])
        return [[fst] + rst for fst in tag_first for rst in tag_rest]

# End of PART B.