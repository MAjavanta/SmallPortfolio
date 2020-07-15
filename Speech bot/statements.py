# File: agreement.py
# Template file for Informatics 2A Assignment 2:
# 'A Natural Language Query System in Python/NLTK'

# John Longley, November 2012
# Revised November 2013 and November 2014 with help from Nikolay Bogoychev
# Revised November 2015 by Toms Bergmanis
# Revised October 2017 by Chunchuan Lyu with help from Kiniorski Filip


# PART C: Syntax and agreement checking

from statements import *
from pos_tagging import *

# Grammar for the query language (with POS tokens as terminals):

from nltk import CFG
from nltk import parse
from nltk import Tree

grammar = CFG.fromstring('''
   S     -> WHO QP QM | WHICH Nom QP QM
   QP    -> VP | DO NP T
   VP    -> I | T NP | BE A | BE NP | VP AND VP
   NP    -> P | AR Nom | Nom
   Nom   -> AN | AN Rel
   AN    -> N | A AN
   Rel   -> WHO VP | NP T
   N     -> "Ns" | "Np"
   I    -> "Is" | "Ip"
   T    -> "Ts" | "Tp"
   A     -> "A"
   P     -> "P"
   BE    -> "BEs" | "BEp"
   DO    -> "DOs" | "DOp"
   AR    -> "AR"
   WHO   -> "WHO"
   WHICH -> "WHICH"
   AND   -> "AND"
   QM    -> "?"
   ''')

chartpsr = parse.ChartParser(grammar)

def all_parses(wlist,lx):
    """returns all possible parse trees for all possible taggings of wlist"""
    allp = []
    for tagging in tag_words(lx, wlist):
        allp = allp + [t for t in chartpsr.parse(tagging)]
    return allp

# This produces parse trees of type Tree.
# Available operations on trees:  tr.label(), tr[i],  len(tr)


# Singular/plural agreement checking.

# For convenience, we reproduce the parameterized rules from the handout here:

#    S      -> WHO QP[y] QM | WHICH Nom[y] QP[y] QM
#    QP[x]  -> VP[x] | DO[y] NP[y] T[p]
#    VP[x]  -> I[x] | T[x] NP | BE[x] A | BE[x] NP[x] | VP[x] AND VP[x]
#    NP[s]  -> P | AR Nom[s]
#    NP[p]  -> Nom[p]
#    Nom[x] -> AN[x] | AN[x] Rel[x]
#    AN[x]  -> N[x] | A AN[x]
#    Rel[x] -> WHO VP[x] | NP[y] T[y]
#    N[s]   -> "Ns"  etc.

def label(t):
    if (isinstance(t,str)):
        return t
    elif (isinstance(t,tuple)):
        return t[1]
    else:
        return t.label()

def top_level_rule(tr):
    if (isinstance(tr,str)):
        return ''
    else:
        rule = tr.label() + ' ->'
        for t in tr:
            rule = rule + ' ' + label(t)
        return rule

def N_phrase_num(tr):
    """returns the number attribute of a noun-like tree, based on its head noun"""
    if (tr.label() == 'N'):
        return tr[0][1]  # the s or p from Ns or Np
    elif (tr.label() == 'Nom'):
        return N_phrase_num(tr[0])
    elif (tr.label() == 'NP'):
        if len(tr) == 1:
            return N_phrase_num(tr[0])
        elif tr[0].label() == 'P':
            return 's'
        else:
            return N_phrase_num(tr[1])
    elif (tr.label() == 'AN'):
        if tr[0].label() == 'A':
            return N_phrase_num(tr[1])
        else:
            return N_phrase_num(tr[0])
    else:
        return ''

def V_phrase_num(tr):
    """returns the number attribute of a verb-like tree, based on its head verb,
       or '' if this is undetermined."""
    if (tr.label() == 'T' or tr.label() == 'I'):
        return tr[0][1]  # the s or p from Is,Ts or Ip,Tp
    elif (tr.label() == 'VP'):
        return V_phrase_num(tr[0])
    elif (tr.label() == 'DO') or (tr.label() == 'BE'):
        return tr[0][2]
    elif (tr.label() == 'QP'):
        if tr[0].label() == 'VP':
            return N_phrase_num(tr[0])
        else:
            return ''
    elif (tr.label() == 'Rel'):
        if tr[0].label() == 'WHO':
            return N_phrase_num(tr[1])
    else:
        return ''


def matches(n1,n2):
    return (n1==n2 or n1=='' or n2=='')


def check_node(tr):
    """checks agreement constraints at the root of tr"""
    rule = top_level_rule(tr)
    if (rule == 'S -> WHICH Nom QP QM'):
        return (matches(N_phrase_num(tr[1]), V_phrase_num(tr[2])))
    elif (rule == 'NP -> AR Nom'):
        return (N_phrase_num(tr[1]) == 's')
    elif (rule == 'NP -> Nom'):
        return (N_phrase_num(tr[0]) == 'p')
    elif (rule == 'QP -> DO NP T'):
        return matches(V_phrase_num(tr[0]), N_phrase_num(tr[1])) and V_phrase_num(tr[2]) == 'p'
    elif (rule == 'VP -> VP AND VP'):
        return matches(V_phrase_num(tr[0]), V_phrase_num(tr[2]))
    elif (rule == 'VP -> BE NP'):
        return matches(V_phrase_num(tr[0]), N_phrase_num(tr[1]))
    elif (rule == 'Nom -> AN Rel') or (rule == 'Rel -> NP T'):
        return matches(N_phrase_num(tr[0]), V_phrase_num(tr[1]))
    else:
        return True


def check_all_nodes(tr):
    """checks agreement constraints everywhere in tr"""
    if (isinstance(tr,str)):
        return True
    elif (not check_node(tr)):
        return False
    else:
        for subtr in tr:
            if (not check_all_nodes(subtr)):
                return False
        return True

def all_valid_parses(lx, wlist):
    """returns all possible parse trees for all possible taggings of wlist
       that satisfy agreement constraints"""
    return [t for t in all_parses(wlist,lx) if check_all_nodes(t)]


# Converter to add words back into trees.
# Strips singular verbs and plural nouns down to their stem.

def restore_words_aux(tr,wds):
    if (isinstance(tr,str)):
        wd = wds.pop()
        if (tr=='Is'):
            return ('I_' + verb_stem(wd), tr)
        elif (tr=='Ts'):
            return ('T_' + verb_stem(wd), tr)
        elif (tr=='Np'):
            return ('N_' + noun_stem(wd), tr)
        elif (tr=='Ip' or tr=='Tp' or tr=='Ns' or tr=='A'):
            return (tr[0] + '_' + wd, tr)
        else:
            return (wd, tr)
    else:
        return Tree(tr.label(), [restore_words_aux(t,wds) for t in tr])

def restore_words(tr,wds):
    """adds words back into syntax tree, sometimes tagged with POS prefixes"""
    wdscopy = wds+[]
    wdscopy.reverse()
    return restore_words_aux(tr,wdscopy)

# Example:

if __name__ == "__main__":
    #code for a simple testing, feel free to modify
    lx = Lexicon()
    lx.add('John','P')
    lx.add('like','T')
    tr0 = all_valid_parses(lx, ['Who','likes','John','?'])[0]
    tr = restore_words(tr0,['Who','likes','John','?'])
    tr.draw()

# End of PART C.

[bruegel]s1710355: cat statements.py
# File: statements.py
# Template file for Informatics 2A Assignment 2:
# 'A Natural Language Query System in Python/NLTK'

# John Longley, November 2012
# Revised November 2013 and November 2014 with help from Nikolay Bogoychev
# Revised November 2015 by Toms Bergmanis
# Revised October 2017 by Chunchuan Lyu


# PART A: Processing statements

def add(lst,item):
    if (item not in lst):
        lst.insert(len(lst),item)


class Lexicon:
    """stores known word stems of various part-of-speech categories"""
    # add code here
    def __init__(self):
        self.data = {'P': [], 'N': [], 'A': [], 'I': [], 'T': []}

    def add(self, stem, cat):
        if not cat in self.data.keys():
            print("Invalid cat")
        else:
            self.data[cat].append(stem)

    def getAll(self, cat):
        output = [word for word in self.data[cat]]
        return list(set(output))


class FactBase:
    """stores unary and binary relational facts"""
    # add code here
    def __init__(self):
        self.unary = []
        self.binary = []

    def addUnary(self, pred, e1):
        self.unary.append((pred, e1))

    def addBinary(self, pred, e1, e2):
        self.binary.append((pred, e1, e2))

    def queryUnary(self, pred, e1):
        for (x, y) in self.unary:
            if x == pred and y == e1:
                return True
        return False

    def queryBinary(self, pred, e1, e2):
        for (x, y, z) in self.binary:
            if x == pred and y == e1 and z == e2:
                return True
        return False


import re
import nltk
from nltk.corpus import brown


verbs_singular = [verb for verb, tag in nltk.corpus.brown.tagged_words() if tag == "VB"]
verbs_plural = [verb for verb, tag in nltk.corpus.brown.tagged_words() if tag == "VBZ"]


def verb_stem(s):
    """extracts the stem from the 3sg form of a verb, or returns empty string"""
    # add code here

    stem = extract_3s(s)

    if stem in verbs_singular or s in verbs_plural:
        return stem
    else:
        return ""


def extract_3s(s):
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
    elif re.match(r'.*[^iosxz]es', s, re.IGNORECASE) and not re.match(r'.*ches', s, re.IGNORECASE) and not re.match(r'.*shes', s, re.IGNORECASE):
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



def add_proper_name (w,lx):
    """adds a name to a lexicon, checking if first letter is uppercase"""
    if ('A' <= w[0] and w[0] <= 'Z'):
        lx.add(w,'P')
        return ''
    else:
        return (w + " isn't a proper name")

def process_statement (lx,wlist,fb):
    """analyses a statement and updates lexicon and fact base accordingly;
       returns '' if successful, or error message if not."""
    # Grammar for the statement language is:
    #   S  -> P is AR Ns | P is A | P Is | P Ts P
    #   AR -> a | an
    # We parse this in an ad hoc way.
    msg = add_proper_name (wlist[0],lx)
    if (msg == ''):
        if (wlist[1] == 'is'):
            if (wlist[2] in ['a','an']):
                lx.add (wlist[3],'N')
                fb.addUnary ('N_'+wlist[3],wlist[0])
            else:
                lx.add (wlist[2],'A')
                fb.addUnary ('A_'+wlist[2],wlist[0])
        else:
            stem = verb_stem(wlist[1])
            if (len(wlist) == 2):
                lx.add (stem,'I')
                fb.addUnary ('I_'+stem,wlist[0])
            else:
                msg = add_proper_name (wlist[2],lx)
                if (msg == ''):
                    lx.add (stem,'T')
                    fb.addBinary ('T_'+stem,wlist[0],wlist[2])
    return msg

# End of PART A.